package com.githubzs.plataforma_reservas_medicas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;
    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;
    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AvailabilityServiceImpl service;

    private UUID doctorId;
    private UUID appointmentTypeId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        appointmentTypeId = UUID.randomUUID();
    }

    @Test
    void shouldGetAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 4, 10);

        DoctorSchedule schedule1 = DoctorSchedule.builder()
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        DoctorSchedule schedule2 = DoctorSchedule.builder()
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        Appointment appointment1 = Appointment.builder()
                .startAt(LocalDateTime.of(date, LocalTime.of(7, 0)))
                .endAt(LocalDateTime.of(date, LocalTime.of(8, 0)))
                .build();

        Appointment appointment2 = Appointment.builder()
                .startAt(LocalDateTime.of(date, LocalTime.of(14, 0)))
                .endAt(LocalDateTime.of(date, LocalTime.of(15, 0)))
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of(schedule1, schedule2));
        
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(6, 0)),
                LocalDateTime.of(date, LocalTime.of(12, 0))))
            .thenReturn(List.of(appointment1));
        
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(13, 0)),
                LocalDateTime.of(date, LocalTime.of(17, 0))))
            .thenReturn(List.of(appointment2));

        List<AvailabilitySlotResponse> slots = service.getAvailableSlots(doctorId, date);

        assertEquals(4, slots.size());

        var starts = slots.stream().map(AvailabilitySlotResponse::slotStart).collect(Collectors.toList());
        assertEquals(List.of(LocalTime.of(6, 0), LocalTime.of(8, 0), LocalTime.of(13, 0), LocalTime.of(15, 0)), starts);
        
        var ends = slots.stream().map(AvailabilitySlotResponse::slotEnd).collect(Collectors.toList());
        assertEquals(List.of(LocalTime.of(7, 0), LocalTime.of(12, 0), LocalTime.of(14, 0), LocalTime.of(17, 0)), ends);

        verify(doctorRepository).existsById(doctorId);
        verify(doctorScheduleRepository).findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek());
        verify(appointmentRepository).findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(6, 0)),
                LocalDateTime.of(date, LocalTime.of(12, 0)));
        verify(appointmentRepository).findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(13, 0)),
                LocalDateTime.of(date, LocalTime.of(17, 0)));
    }

    @Test
    void shouldReturnSingleFreeSlotForGetAvailableSlotsWhenNoAppointments() {
        LocalDate date = LocalDate.of(2026, 4, 10);

        DoctorSchedule schedule = DoctorSchedule.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(8, 0)),
                LocalDateTime.of(date, LocalTime.of(17, 0))))
                .thenReturn(List.of());

        List<AvailabilitySlotResponse> slots = service.getAvailableSlots(doctorId, date);

        assertFalse(slots.isEmpty());
        assertEquals(1, slots.size());
        assertEquals(date, slots.get(0).date());
        assertEquals(LocalTime.of(8, 0), slots.get(0).slotStart());
        assertEquals(LocalTime.of(17, 0), slots.get(0).slotEnd());
        verify(doctorRepository).existsById(doctorId);
        verify(doctorScheduleRepository).findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek());
        verify(appointmentRepository).findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(8, 0)),
                LocalDateTime.of(date, LocalTime.of(17, 0)));
    }

    @Test
    void shouldSubtractAppointmentsAndIgnoreCancelledForGetAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 4, 10);

        DoctorSchedule schedule = DoctorSchedule.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        Appointment confirmed = Appointment.builder()
                .startAt(LocalDateTime.of(date, LocalTime.of(9, 0)))
                .endAt(LocalDateTime.of(date, LocalTime.of(10, 0)))
                .status(AppointmentStatus.CONFIRMED)
                .build();
        Appointment cancelled = Appointment.builder()
                .startAt(LocalDateTime.of(date, LocalTime.of(10, 30)))
                .endAt(LocalDateTime.of(date, LocalTime.of(11, 0)))
                .status(AppointmentStatus.CANCELLED)
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(8, 0)),
                LocalDateTime.of(date, LocalTime.of(12, 0))))
                .thenReturn(List.of(confirmed, cancelled));

        List<AvailabilitySlotResponse> slots = service.getAvailableSlots(doctorId, date);

        assertEquals(2, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).slotStart());
        assertEquals(LocalTime.of(9, 0), slots.get(0).slotEnd());
        assertEquals(LocalTime.of(10, 0), slots.get(1).slotStart());
        assertEquals(LocalTime.of(12, 0), slots.get(1).slotEnd());
    }

    @Test
    void shouldReturnEmptyForGetAvailableSlotsWhenNoDoctorSchedule() {
        LocalDate date = LocalDate.of(2026, 4, 10);

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of());

        List<AvailabilitySlotResponse> slots = service.getAvailableSlots(doctorId, date);

        assertEquals(0, slots.size());
        verify(appointmentRepository, never()).findByDoctorIdAndStartAtBetweenExcludeTo(any(), any(), any());
    }

    @Test
    void shouldThrowNotFoundForGetAvailableSlotsWhenDoctorDoesNotExist() {
        LocalDate date = LocalDate.of(2026, 4, 10);

        when(doctorRepository.existsById(doctorId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getAvailableSlots(doctorId, date));
        verify(doctorScheduleRepository, never()).findByDoctor_IdAndDayOfWeek(any(), any());
    }

    @Test
    void shouldReturnChunkedSlotsForGetAvailableSlotsForAppointmentType() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        DoctorSchedule schedule = DoctorSchedule.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(8, 0)),
                LocalDateTime.of(date, LocalTime.of(10, 0))))
                .thenReturn(List.of());

        List<AvailabilitySlotResponse> slots = service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId);

        assertEquals(4, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).slotStart());
        assertEquals(LocalTime.of(8, 30), slots.get(0).slotEnd());
        assertEquals(LocalTime.of(9, 30), slots.get(3).slotStart());
        assertEquals(LocalTime.of(10, 0), slots.get(3).slotEnd());
        verify(appointmentTypeRepository).findById(appointmentTypeId);
    }

    @Test
    void shouldReturnEmptyWhenDurationGreaterThanFreeSlotForGetAvailableSlotsForAppointmentType() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(60).build();

        DoctorSchedule schedule = DoctorSchedule.builder()
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of(schedule));

        List<AvailabilitySlotResponse> slots = service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId);

        assertEquals(0, slots.size());
    }

    @Test
    void shouldReturnEmptyForGetAvailableSlotsForAppointmentTypeWhenNoDoctorSchedule() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of());

        List<AvailabilitySlotResponse> slots = service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId);

        assertEquals(0, slots.size());
    }

    @Test
    void shouldThrowNotFoundForGetAvailableSlotsForAppointmentTypeWhenTypeDoesNotExist() {
        LocalDate date = LocalDate.of(2026, 4, 10);

        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId));
        verify(doctorRepository, never()).existsById(any());
    }

    @Test
    void shouldThrowValidationExceptionForGetAvailableSlotsForAppointmentTypeWhenDurationIsNonPositive() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(0).build();

        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));

        assertThrows(ValidationException.class,
                () -> service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId));
        verify(doctorRepository, never()).existsById(any());
    }

    @Test
    void shouldThrowValidationExceptionForGetAvailableSlotsForAppointmentTypeWhenDurationExceeds480() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(600).build();

        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));

        assertThrows(ValidationException.class,
                () -> service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId));
        verify(doctorRepository, never()).existsById(any());
    }

}
