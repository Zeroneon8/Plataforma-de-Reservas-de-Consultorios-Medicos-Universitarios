package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.githubzs.plataforma_reservas_medicas.services.DoctorScheduleService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorScheduleMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorScheduleSummaryMapper;
import com.githubzs.plataforma_reservas_medicas.api.error.ErrorResponse.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleMapper mapper;
    private final DoctorScheduleSummaryMapper summaryMapper;


    @Override
    @Transactional
    public DoctorScheduleResponse create(UUID doctorId, DoctorScheduleCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Doctor schedule request is required",
                List.of(new FieldViolation("request", "is required")));
        }
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new ConflictException("Doctor is not active");
        }
        if (request.dayOfWeek() == null) {
            throw new ValidationException("Day of week is required",
                List.of(new FieldViolation("dayOfWeek", "is required")));
        }
        if (request.startTime() == null || request.endTime() == null) {
            throw new ValidationException("Start time and end time are required",
                List.of(new FieldViolation("startTime", "is required"), new FieldViolation("endTime", "is required")));
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new ValidationException("Schedule start time must be before end time",
                List.of(new FieldViolation("startTime", "must be before endTime")));
        }

        validateNoOverlap(doctorId, request.dayOfWeek(), request.startTime(), request.endTime());

        DoctorSchedule schedule = mapper.toEntity(request);
        schedule.setDoctor(doctor);

        DoctorSchedule saved = scheduleRepository.save(schedule);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleSummaryResponse> findByDoctor(UUID doctorId) {
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctor_Id(doctorId);

        sortSchedules(schedules);
        return schedules.stream()
                .map(summaryMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleSummaryResponse> findByDoctorAndDay(UUID doctorId, DayOfWeek day) {
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        if (day == null) {
            throw new ValidationException("Day of week is required",
                List.of(new FieldViolation("dayOfWeek", "is required")));
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, day);

        sortSchedules(schedules);
        return schedules.stream()
                .map(summaryMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWithinSchedule(UUID doctorId, LocalDateTime start, LocalDateTime end) {
        if (doctorId == null) {
            throw new ValidationException("Doctor id is required",
                List.of(new FieldViolation("doctorId", "is required")));
        }
        if (start == null) {
            throw new ValidationException("Start time is required",
                List.of(new FieldViolation("startTime", "is required")));
        }
        if (end == null) {
            throw new ValidationException("End time is required",
                List.of(new FieldViolation("endTime", "is required")));
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }
        if (!start.isBefore(end)) {
            return false;
        }
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return false;
        }

        LocalTime startTime = start.toLocalTime();
        LocalTime endTime   = end.toLocalTime();
        DayOfWeek day       = start.getDayOfWeek();

        return scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, day).stream()
                .anyMatch(s -> !startTime.isBefore(s.getStartTime())
                            && !endTime.isAfter(s.getEndTime()));
    }


    private void validateNoOverlap(UUID doctorId, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, day).stream()
                .filter(existing -> overlaps(existing.getStartTime(), existing.getEndTime(), startTime, endTime))
                .findFirst()
                .ifPresent(existing -> {
                    throw new ConflictException("Doctor schedule overlaps with existing availability from "
                            + existing.getStartTime() + " to " + existing.getEndTime());
                });
    }

    private boolean overlaps(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }

    private void sortSchedules(List<DoctorSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) return;
        schedules.sort(
            Comparator.comparing(DoctorSchedule::getStartTime)
                    .thenComparing(DoctorSchedule::getEndTime)
        );
    }

}
