package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
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
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.services.AvailabilityService;

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
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        if (date == null) {
            throw new ValidationException("Date is required",
                List.of(new FieldViolation("date", "is required")));
        }

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
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        if (date == null) {
            throw new ValidationException("Date is required",
                List.of(new FieldViolation("date", "is required")));
        }
        if (appointmentTypeId == null) {
            throw new ValidationException("Appointment type id is required",
                List.of(new FieldViolation("appointmentTypeId", "is required")));
        }

        AppointmentType appointmentType = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + appointmentTypeId));

        int durationMinutes = appointmentType.getDurationMinutes();

        if (durationMinutes <= 0) {
            throw new ValidationException("Invalid appointment type duration",
                List.of(new FieldViolation("durationMinutes", "must be a positive integer")));
        }
        else if (durationMinutes > 480) {
            throw new ValidationException("Invalid appointment type duration",
                List.of(new FieldViolation("durationMinutes", "must be less than or equal to 480")));
        }


        List<AvailabilitySlotResponse> freeSlots = getAvailableSlots(doctorId, date);
        List<AvailabilitySlotResponse> availableSlots = new ArrayList<>();

        for (AvailabilitySlotResponse free : freeSlots) {
            LocalDateTime pivot = LocalDateTime.of(free.date(), free.slotStart());
            LocalDateTime freeEnd = LocalDateTime.of(free.date(), free.slotEnd());

            while (!pivot.plusMinutes(durationMinutes).isAfter(freeEnd)) {
                LocalDateTime chunkEnd = pivot.plusMinutes(durationMinutes);
                availableSlots.add(new AvailabilitySlotResponse(
                        free.date(),
                        pivot.toLocalTime(),
                        chunkEnd.toLocalTime()
                ));
                pivot = chunkEnd;
            }
        }
        
        sortSlots(availableSlots);
        return availableSlots;
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
