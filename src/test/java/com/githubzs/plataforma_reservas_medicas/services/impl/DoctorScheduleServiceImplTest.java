package com.githubzs.plataforma_reservas_medicas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
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

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorScheduleRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorScheduleMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorScheduleSummaryMapperImpl;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @InjectMocks
    private DoctorScheduleServiceImpl service;

    private UUID doctorId;

    @BeforeEach
    // Setea mappers reales para evitar falsos positivos por mockeo de MapStruct.
    void setUp() {
        doctorId = UUID.randomUUID();

        var mapperImpl = new DoctorScheduleMapperImpl();
        var summaryMapperImpl = new DoctorScheduleSummaryMapperImpl();

        setField(service, "mapper", mapperImpl);
        setField(service, "summaryMapper", summaryMapperImpl);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
            return;
        } catch (NoSuchFieldException e) {
            Class<?> cls = target.getClass().getSuperclass();
            while (cls != null) {
                try {
                    Field f = cls.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException ex) {
                    cls = cls.getSuperclass();
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateDoctorScheduleWhenAllValidationsPass() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        UUID scheduleId = UUID.randomUUID();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of());
        when(scheduleRepository.save(any(DoctorSchedule.class))).thenAnswer(inv -> {
            DoctorSchedule schedule = inv.getArgument(0);
            schedule.setId(scheduleId);
            return schedule;
        });

        DoctorScheduleResponse result = service.create(doctorId, request);

        assertNotNull(result);
        assertEquals(scheduleId, result.id());
        assertEquals(DayOfWeek.MONDAY, result.dayOfWeek());
        assertEquals(LocalTime.of(8, 0), result.startTime());
        assertEquals(LocalTime.of(12, 0), result.endTime());
        assertEquals(doctorId, result.doctor().id());
        verify(scheduleRepository).save(any(DoctorSchedule.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenDoctorIdIsNullForCreate() {
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertThrows(ValidationException.class, () -> service.create(null, request));
    }

    @Test
    void shouldThrowValidationExceptionWhenRequestIsNullForCreate() {
        assertThrows(ValidationException.class, () -> service.create(doctorId, null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForCreate() {
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(doctorId, request));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenDoctorIsInactiveForCreate() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.INACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(ConflictException.class, () -> service.create(doctorId, request));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenDayOfWeekIsNullForCreate() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(null, LocalTime.of(8, 0), LocalTime.of(12, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(ValidationException.class, () -> service.create(doctorId, request));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenStartOrEndIsNullForCreate() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), null);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(ValidationException.class, () -> service.create(doctorId, request));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenStartTimeIsNotBeforeEndTimeForCreate() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(8, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(ValidationException.class, () -> service.create(doctorId, request));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenScheduleOverlapsForCreate() {
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        DoctorSchedule existing = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0)).build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> service.create(doctorId, request));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldReturnTrueWhenRangeFitsInsideScheduleForIsWithinSchedule() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 6, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 6, 11, 0);
        DoctorSchedule schedule = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(schedule));

        assertEquals(true, service.isWithinSchedule(doctorId, start, end));
    }

    @Test
    void shouldReturnFalseWhenRangeOutsideScheduleForIsWithinSchedule() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 6, 7, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 6, 9, 0);
        DoctorSchedule schedule = DoctorSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of(schedule));

        assertFalse(service.isWithinSchedule(doctorId, start, end));
    }

    @Test
    void shouldReturnFalseWhenStartIsNotBeforeEndForIsWithinSchedule() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 6, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 6, 10, 0);

        when(doctorRepository.existsById(doctorId)).thenReturn(true);

        assertFalse(service.isWithinSchedule(doctorId, start, end));
        verify(scheduleRepository, never()).findByDoctor_IdAndDayOfWeek(any(), any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForIsWithinSchedule() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 6, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 6, 9, 0);

        when(doctorRepository.existsById(doctorId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.isWithinSchedule(doctorId, start, end));
    }

    @Test
    void shouldReturnSortedSchedulesForFindByDoctorAndDay() {
        DoctorSchedule scheduleLater = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();
        DoctorSchedule scheduleEarlier = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, DayOfWeek.MONDAY))
            .thenReturn(new ArrayList<>(List.of(scheduleLater, scheduleEarlier)));

        List<DoctorScheduleSummaryResponse> results = service.findByDoctorAndDay(doctorId, DayOfWeek.MONDAY);

        assertEquals(2, results.size());
        assertEquals(scheduleEarlier.getId(), results.get(0).id());
        assertEquals(scheduleLater.getId(), results.get(1).id());
    }

    @Test
    void shouldThrowValidationExceptionWhenDoctorIdIsNullForFindByDoctorAndDay() {
        assertThrows(ValidationException.class, () -> service.findByDoctorAndDay(null, DayOfWeek.MONDAY));
    }

    @Test
    void shouldThrowValidationExceptionWhenDayIsNullForFindByDoctorAndDay() {
        assertThrows(ValidationException.class, () -> service.findByDoctorAndDay(doctorId, null));
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForFindByDoctorAndDay() {
        when(doctorRepository.existsById(doctorId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.findByDoctorAndDay(doctorId, DayOfWeek.MONDAY));
    }

    @Test
    void shouldReturnSortedSchedulesForFindByDoctor() {
        DoctorSchedule scheduleLater = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();
        DoctorSchedule scheduleEarlier = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .build();

        when(doctorRepository.existsById(doctorId)).thenReturn(true);
        when(scheduleRepository.findByDoctor_Id(doctorId))
            .thenReturn(new ArrayList<>(List.of(scheduleLater, scheduleEarlier)));

        List<DoctorScheduleSummaryResponse> results = service.findByDoctor(doctorId);

        assertEquals(2, results.size());
        assertEquals(scheduleEarlier.getId(), results.get(0).id());
        assertEquals(scheduleLater.getId(), results.get(1).id());
    }

    @Test
    void shouldThrowResourceNotFoundWhenDoctorDoesNotExistForFindByDoctor() {
        when(doctorRepository.existsById(doctorId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.findByDoctor(doctorId));
    }

}
