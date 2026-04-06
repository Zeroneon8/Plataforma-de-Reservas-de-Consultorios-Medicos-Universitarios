package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.AvailabilitySlotResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorScheduleRepository;

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
    void getAvailableSlotsShouldReturnSlotsWhenDoctorHasSchedule() {
        LocalDate date = LocalDate.now().plusDays(1);
        AppointmentType type = AppointmentType.builder()
                .id(appointmentTypeId)
                .durationMinutes(30)
                .build();

        DoctorSchedule schedule = DoctorSchedule.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenExcludeTo(
                doctorId,
                LocalDateTime.of(date, LocalTime.of(8, 0)),
                LocalDateTime.of(date, LocalTime.of(17, 0))))
                .thenReturn(new ArrayList<>());

        List<AvailabilitySlotResponse> slots = service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId);

        assertFalse(slots.isEmpty());
        assertEquals(date, slots.get(0).date());
    }

    @Test
    void getAvailableSlotsShouldReturnEmptyWhenNoDoctorSchedule() {
        LocalDate date = LocalDate.now().plusDays(1);
        AppointmentType type = AppointmentType.builder()
                .id(appointmentTypeId)
                .durationMinutes(30)
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, date.getDayOfWeek()))
                .thenReturn(new ArrayList<>());

        List<AvailabilitySlotResponse> slots = service.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId);

        assertEquals(0, slots.size());
    }

}
