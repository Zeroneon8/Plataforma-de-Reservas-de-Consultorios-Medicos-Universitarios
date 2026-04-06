package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.AvailabilitySlotResponse;
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

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getAvailableSlots(
            UUID doctorId,
            LocalDate date,
            UUID appointmentTypeId) {

        Objects.requireNonNull(doctorId, "Doctor id is required");
        Objects.requireNonNull(date, "Date is required");
        Objects.requireNonNull(appointmentTypeId, "Appointment type id is required");

        
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }

        
        AppointmentType appointmentType = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + appointmentTypeId));

        int durationMinutes = appointmentType.getDurationMinutes();

        // Obtener el horario laboral del doctor para ese día
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, dayOfWeek);

        if (schedules.isEmpty()) {
            return new ArrayList<>();
        }

        List<AvailabilitySlotResponse> availableSlots = new ArrayList<>();

        // Para cada bloque de horario laboral
        for (DoctorSchedule schedule : schedules) {
            // Obtener citas existentes del doctor en esa fecha dentro de ese horario
            LocalDateTime scheduleStart = LocalDateTime.of(date, schedule.getStartTime());
            LocalDateTime scheduleEnd = LocalDateTime.of(date, schedule.getEndTime());

            List<Appointment> existingAppointments = appointmentRepository
                    .findByDoctorIdAndStartAtBetweenExcludeTo(doctorId, scheduleStart, scheduleEnd);

            // Calcular slots libres
            availableSlots.addAll(calculateAvailableSlots(
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    existingAppointments,
                    date,
                    durationMinutes));
        }

        return availableSlots;
    }

    private List<AvailabilitySlotResponse> calculateAvailableSlots(
            LocalTime scheduleStart,
            LocalTime scheduleEnd,
            List<Appointment> existingAppointments,
            LocalDate date,
            int durationMinutes) {

        List<AvailabilitySlotResponse> slots = new ArrayList<>();

        // Ordena las citas existentes por hora de inicio
        List<Appointment> sortedAppointments = existingAppointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .sorted((a, b) -> a.getStartAt().compareTo(b.getStartAt()))
                .toList();

        LocalTime currentTime = scheduleStart;

        for (Appointment appointment : sortedAppointments) {
            LocalTime appointmentStart = appointment.getStartAt().toLocalTime();
            LocalTime appointmentEnd = appointment.getEndAt().toLocalTime();

            // Generar slots desde currentTime hasta appointmentStart
            while (currentTime.isBefore(appointmentStart) &&
                    currentTime.plusMinutes(durationMinutes).compareTo(appointmentStart) <= 0) {
                LocalTime slotEnd = currentTime.plusMinutes(durationMinutes);
                slots.add(new AvailabilitySlotResponse(date, currentTime, slotEnd));
                currentTime = slotEnd;
            }

            // Mover currentTime al final de la cita existente
            currentTime = appointmentEnd.isAfter(currentTime) ? appointmentEnd : currentTime;
        }

        // Generar slots finales desde currentTime hasta scheduleEnd
        while (currentTime.isBefore(scheduleEnd) &&
                currentTime.plusMinutes(durationMinutes).compareTo(scheduleEnd) <= 0) {
            LocalTime slotEnd = currentTime.plusMinutes(durationMinutes);
            slots.add(new AvailabilitySlotResponse(date, currentTime, slotEnd));
            currentTime = slotEnd;
        }

        return slots;
    }

}
