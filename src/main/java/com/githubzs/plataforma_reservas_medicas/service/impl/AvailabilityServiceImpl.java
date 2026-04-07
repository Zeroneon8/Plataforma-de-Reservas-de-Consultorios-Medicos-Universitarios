package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.AvailabilitySlotResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.TimeRange;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorScheduleRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final DoctorRepository doctorRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;

    // La implementación actual no toma en cuenta citas que iniciaron el dia anterior pero terminan el dia consultado
    @Override
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getAvailableSlots(UUID doctorId, LocalDate date) {
        Objects.requireNonNull(doctorId, "Doctor id is required");
        Objects.requireNonNull(date, "Date is required");

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, dayOfWeek);
        
        if (schedules.isEmpty()) {
            return List.of();
        }

        List<AvailabilitySlotResponse> availableSlots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            LocalDateTime scheduleStart = LocalDateTime.of(date, schedule.getStartTime());
            LocalDateTime scheduleEnd = LocalDateTime.of(date, schedule.getEndTime());

            if (!scheduleStart.isBefore(scheduleEnd)) {
                continue; 
            }

            List<Appointment> appointments = appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(doctorId, scheduleStart, scheduleEnd);

            List<TimeRange> freeSlots = subtractAppointments(scheduleStart, scheduleEnd, appointments);

            for (TimeRange slot : freeSlots) {
                availableSlots.add(new AvailabilitySlotResponse(date, slot.start().toLocalTime(), slot.end().toLocalTime()));
            }
        }

        sortSlots(availableSlots);
        return availableSlots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getAvailableSlotsForAppointmentType (UUID doctorId, LocalDate date, UUID appointmentTypeId) {
        Objects.requireNonNull(doctorId, "Doctor id is required");
        Objects.requireNonNull(date, "Date is required");
        Objects.requireNonNull(appointmentTypeId, "Appointment type id is required");

        AppointmentType appointmentType = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + appointmentTypeId));

        int durationMinutes = appointmentType.getDurationMinutes();

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Appointment type duration must be greater than 0");
        }
        else if (durationMinutes > 480) {
            throw new IllegalArgumentException("Appointment type duration is too long");
        }


        List<AvailabilitySlotResponse> freeSlots = getAvailableSlots(doctorId, date);
        List<AvailabilitySlotResponse> avaibleSlots = new ArrayList<>();

        for (AvailabilitySlotResponse free : freeSlots) {
            LocalDateTime pivot = LocalDateTime.of(free.date(), free.slotStart());
            LocalDateTime freeEnd = LocalDateTime.of(free.date(), free.slotEnd());

            while (!pivot.plusMinutes(durationMinutes).isAfter(freeEnd)) {
                LocalDateTime chunkEnd = pivot.plusMinutes(durationMinutes);
                avaibleSlots.add(new AvailabilitySlotResponse(
                        free.date(),
                        pivot.toLocalTime(),
                        chunkEnd.toLocalTime()
                ));
                pivot = chunkEnd;
            }
        }
        
        sortSlots(avaibleSlots);
        return avaibleSlots;
    }

    private List<TimeRange> subtractAppointments(LocalDateTime scheduleStart, LocalDateTime scheduleEnd, List<Appointment> appointments) {
        
        // Ordena las citas y filtra las canceladas
        List<Appointment> sorted = appointments.stream()
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
            .sorted((a, b) -> a.getStartAt().compareTo(b.getStartAt()))
            .toList();

        List<TimeRange> freeSlots = new ArrayList<>();
        LocalDateTime pivot = scheduleStart;

        for (Appointment a : sorted) {
            LocalDateTime aStart = a.getStartAt();
            LocalDateTime aEnd = a.getEndAt().isBefore(scheduleEnd) ? a.getEndAt() : scheduleEnd;

            if (!aStart.isBefore(aEnd)){
                continue;
            }

            if (pivot.isBefore(aStart)) {
                freeSlots.add(new TimeRange(pivot, aStart));
            }

            if (pivot.isBefore(aEnd)) {
                pivot = aEnd;
            }
        }

        if (pivot.isBefore(scheduleEnd)) {
            freeSlots.add(new TimeRange(pivot, scheduleEnd));
        }

        return freeSlots;
    }

    private void sortSlots(List<AvailabilitySlotResponse> slots) {
        slots.sort(
            Comparator.comparing(AvailabilitySlotResponse::date)
                    .thenComparing(AvailabilitySlotResponse::slotStart)
                    .thenComparing(AvailabilitySlotResponse::slotEnd)
        );
    }

}
