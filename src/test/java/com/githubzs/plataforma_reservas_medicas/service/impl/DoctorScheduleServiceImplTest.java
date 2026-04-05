package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorScheduleRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.service.mapper.DoctorScheduleMapper;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @Mock
    private DoctorScheduleMapper mapper;

    @InjectMocks
    private DoctorScheduleServiceImpl service;

    private UUID doctorId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
    }

    @Test
    void createShouldSaveScheduleWhenDoctorIsActiveAndNoOverlap() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        DoctorSchedule entity = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();
        DoctorSchedule saved = DoctorSchedule.builder().id(UUID.randomUUID()).dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();
        DoctorScheduleResponse response = new DoctorScheduleResponse(saved.getId(), null, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of());
        when(mapper.toEntity(request)).thenReturn(entity);
        when(scheduleRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        DoctorScheduleResponse result = service.create(doctorId, request);

        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.dayOfWeek());
        verify(scheduleRepository).save(entity);
    }

    @Test
    void createShouldRejectInactiveDoctor() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.INACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(ConflictException.class, () -> service.create(doctorId, request));
    }

    @Test
    void createShouldRejectOverlappingSchedule() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        DoctorSchedule existing = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0)).build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> service.create(doctorId, request));
    }

    @Test
    void isWithinScheduleShouldReturnTrueWhenRangeFitsInsideBlock() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 6, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 6, 11, 0);
        DoctorSchedule schedule = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(schedule));

        assertEquals(true, service.isWithinSchedule(doctorId, start, end));
    }

    @Test
    void isWithinScheduleShouldReturnFalseWhenRangeOutsideBlock() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 6, 7, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 6, 9, 0);
        DoctorSchedule schedule = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(schedule));

        assertFalse(service.isWithinSchedule(doctorId, start, end));
    }

    @Test
    void findByDoctorAndDayShouldReturnMappedSchedules() {
        DoctorSchedule schedule = DoctorSchedule.builder().id(UUID.randomUUID()).dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();
        DoctorScheduleResponse response = new DoctorScheduleResponse(schedule.getId(), null, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(schedule));
        when(mapper.toResponse(schedule)).thenReturn(response);

        List<DoctorScheduleResponse> results = service.findByDoctorAndDay(doctorId, DayOfWeek.MONDAY);

        assertEquals(1, results.size());
        assertEquals(schedule.getId(), results.get(0).id());
    }

}
