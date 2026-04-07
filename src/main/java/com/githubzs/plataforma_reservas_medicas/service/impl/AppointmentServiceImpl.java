package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSearchRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.service.AppointmentService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentSummaryMapper;
import com.githubzs.plataforma_reservas_medicas.service.validator.AppointmentValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper mapper;
    private final AppointmentSummaryMapper summaryMapper;
    private final AppointmentValidator validator;

    @Override
    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        Objects.requireNonNull(request, "Appointment create request is required");

        Patient patient = validator.validatePatientExistsAndActive(request.patientId());
        Doctor doctor = validator.validateDoctorExistsAndActive(request.doctorId());
        Office office = validator.validateOfficeExistsAndAvailable(request.officeId());
        AppointmentType appointmentType = validator.validateAppointmentTypeExists(request.appointmentTypeId());

        // Validar que la fecha de inicio no es pasada y es menor que la fecha de fin
        validator.validateAppointmentStartAtEndAt(request.startAt(), request.startAt().plusMinutes(appointmentType.getDurationMinutes()));

        // Validar que la cita cae dentro del horario laboral del doctor
        LocalDateTime endAt = request.startAt().plusMinutes(appointmentType.getDurationMinutes());
        validator.validateAppointmentWithinDoctorSchedule(request.doctorId(), request.startAt(), endAt);

        // Validar disponibilidad de doctor, consultorio y paciente para el rango horario solicitado
        validator.validateNoOverlapForDoctor(request.doctorId(), request.startAt(), endAt);
        validator.validateNoOverlapForOffice(request.officeId(), request.startAt(), endAt);
        validator.validateNoOverlapForPatient(request.patientId(), request.startAt(), endAt);

        // Crear la cita con estado inicial SCHEDULED
        Appointment appointment = mapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setOffice(office);
        appointment.setAppointmentType(appointmentType);
        appointment.setStartAt(request.startAt());
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(Instant.now());

        Appointment saved = appointmentRepository.save(appointment);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        Objects.requireNonNull(id, "Appointment id is required");

        Appointment appointment = validator.validateAppointmentExists(id);

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> findAll(AppointmentSearchRequest request, Pageable pageable) {
        var requestCopy = request == null ? new AppointmentSearchRequest(null, null, null, null, null, null, null) : request;
        
        if (requestCopy.startAt() != null && requestCopy.endAt() != null && requestCopy.startAt().isAfter(requestCopy.endAt())) {
            throw new ConflictException("Start date cannot be after end date");
        }

        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;

        Specification<Appointment> spec = (root, query, cb) -> cb.conjunction();

        if (requestCopy.patientId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("patient").get("id"), requestCopy.patientId()));
        }
        if (requestCopy.doctorId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("doctor").get("id"), requestCopy.doctorId()));
        }
        if (requestCopy.officeId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("office").get("id"), requestCopy.officeId()));
        }
        if (requestCopy.specialtyId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("doctor").get("specialty").get("id"), requestCopy.specialtyId()));
        }
        if (requestCopy.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), requestCopy.status()));
        }
        if (requestCopy.startAt() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), requestCopy.startAt()));
        }
        if (requestCopy.endAt() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("startAt"), requestCopy.endAt()));
        }

        return appointmentRepository.findAll(spec, finalPageable).map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public AppointmentSummaryResponse confirm(UUID id) {
        Objects.requireNonNull(id, "Appointment id is required");

        Appointment appointment = validator.validateAppointmentExists(id);

        // Solo una cita en estado SCHEDULED puede pasar a CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Only SCHEDULED appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return summaryMapper.toSummaryResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentSummaryResponse cancel(UUID id, AppointmentCancelRequest request) {
        Objects.requireNonNull(id, "Appointment id is required");
        Objects.requireNonNull(request, "Cancel request is required");

        Appointment appointment = validator.validateAppointmentExists(id);

        // Solo se pueden cancelar citas en estado SCHEDULED o CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ConflictException("Only SCHEDULED or CONFIRMED appointments can be cancelled");
        }

        // La cancelación debe registrar un motivo obligatorio
        appointment.setCancelReason(request.cancelReason().trim());
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return summaryMapper.toSummaryResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentSummaryResponse complete(UUID id, AppointmentCompleteRequest request) {
        Objects.requireNonNull(id, "Appointment id is required");
        Objects.requireNonNull(request, "Complete request is required");

        Appointment appointment = validator.validateAppointmentExists(id);

        // Solo una cita CONFIRMED puede pasar a COMPLETED
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ConflictException("Only CONFIRMED appointments can be completed");
        }

        // No se puede completar una cita si la hora actual es anterior al inicio programado
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getStartAt())) {
            throw new ConflictException("Appointment cannot be completed before its scheduled start time");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        if (request.observations() != null && !request.observations().isBlank()) {
            appointment.setObservations(request.observations().trim());
        }
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return summaryMapper.toSummaryResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentSummaryResponse markNoShow(UUID id) {
        Objects.requireNonNull(id, "Appointment id is required");

        Appointment appointment = validator.validateAppointmentExists(id);

        // Solo una cita CONFIRMED puede pasar a NO_SHOW
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ConflictException("Only CONFIRMED appointments can be marked as NO_SHOW");
        }

        // No se puede marcar una cita como NO_SHOW antes de su hora de inicio
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getStartAt())) {
            throw new ConflictException("Appointment cannot be marked as NO_SHOW before its scheduled start time");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return summaryMapper.toSummaryResponse(saved);
    }

}
