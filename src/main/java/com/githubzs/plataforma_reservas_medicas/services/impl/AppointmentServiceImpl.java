package com.githubzs.plataforma_reservas_medicas.services.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.services.AppointmentService;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentSummaryMapper;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentStatusUpdateMapper;
import com.githubzs.plataforma_reservas_medicas.services.validator.AppointmentValidator;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper mapper;
    private final AppointmentSummaryMapper summaryMapper;
    private final AppointmentStatusUpdateMapper statusUpdateMapper;
    private final AppointmentValidator validator;

    @Override
    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Appointment create request is required",
                List.of(new FieldViolation("request", "is required")));
        }

        Patient patient = validator.validatePatientExistsAndActive(request.patientId());
        Doctor doctor = validator.validateDoctorExistsAndActive(request.doctorId());
        Office office = validator.validateOfficeExistsAndAvailable(request.officeId());
        AppointmentType appointmentType = validator.validateAppointmentTypeExists(request.appointmentTypeId());

        // Validar que la fecha de inicio no es pasada y es menor que la fecha de fin
        LocalDateTime startAt = request.startAt();
        LocalDateTime endAt = startAt == null ? null : startAt.plusMinutes(appointmentType.getDurationMinutes());
        validator.validateAppointmentStartAtEndAt(startAt, endAt);

        // Validar que la cita cae dentro del horario laboral del doctor
        validator.validateAppointmentWithinDoctorSchedule(request.doctorId(), startAt, endAt);

        // Validar disponibilidad de doctor, consultorio y paciente para el rango horario solicitado
        validator.validateNoOverlapForDoctor(request.doctorId(), startAt, endAt);
        validator.validateNoOverlapForOffice(request.officeId(), startAt, endAt);
        validator.validateNoOverlapForPatient(request.patientId(), startAt, endAt);

        // Crear la cita con estado inicial SCHEDULED
        Appointment appointment = mapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setOffice(office);
        appointment.setAppointmentType(appointmentType);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(Instant.now());

        Appointment saved = appointmentRepository.save(appointment);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        if (id == null) {
            throw new ValidationException("Appointment id is required",
                List.of(new FieldViolation("id", "is required")));
        }

        Appointment appointment = validator.validateAppointmentExists(id);

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> findByDoctorDocumentNumber(String documentNumber, Pageable pageable) {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new ValidationException("Doctor document number is required",
                List.of(new FieldViolation("documentNumber", "is required")));
        }

        String normalizedDocumentNumber = documentNumber.trim();
        Doctor doctor = validator.validateDoctorExistsAndActiveByDocumentNumber(normalizedDocumentNumber);

        Pageable finalPageable = pageable == null ? Pageable.ofSize(10) : pageable;

        return appointmentRepository.findByDoctor_DocumentNumber(doctor.getDocumentNumber(), finalPageable)
            .map(summaryMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(AppointmentSearchRequest request, Pageable pageable) {
        AppointmentSearchRequest requestCopy = request == null ? new AppointmentSearchRequest(null, null, null, null, null, null, null) : request;
        
        if (requestCopy.startAt() != null && requestCopy.endAt() != null && requestCopy.startAt().isAfter(requestCopy.endAt())) {
            throw new ValidationException("Start time must be before end time",
                List.of(new FieldViolation("startAt", "must be before or equal to endAt")));
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

        return appointmentRepository.findAll(spec, finalPageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentStatusUpdateResponse confirm(UUID id) {
        if (id == null) {
            throw new ValidationException("Appointment id is required",
                List.of(new FieldViolation("id", "is required")));
        }

        Appointment appointment = validator.validateAppointmentExists(id);

        // Solo una cita en estado SCHEDULED puede pasar a CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Only SCHEDULED appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return statusUpdateMapper.toStatusUpdateResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentStatusUpdateResponse cancel(UUID id, AppointmentCancelRequest request) {
        if (id == null) {
            throw new ValidationException("Appointment id is required",
                List.of(new FieldViolation("id", "is required")));
        }
        if (request == null) {
            throw new ValidationException("Cancel request is required",
                List.of(new FieldViolation("request", "is required")));
        }

        // La cancelación debe registrar un motivo obligatorio
        String reason = request.cancelReason();
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Cancel reason is required",
                List.of(new FieldViolation("cancelReason", "is required")));
        }

        Appointment appointment = validator.validateAppointmentExists(id);

        // Solo se pueden cancelar citas en estado SCHEDULED o CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ConflictException("Only SCHEDULED or CONFIRMED appointments can be cancelled");
        }

        appointment.setCancelReason(request.cancelReason().trim());
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return statusUpdateMapper.toStatusUpdateResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentStatusUpdateResponse complete(UUID id, AppointmentCompleteRequest request) {
        if (id == null) {
            throw new ValidationException("Appointment id is required",
                List.of(new FieldViolation("id", "is required")));
        }
        if (request == null) {
            throw new ValidationException("Complete request is required",
                List.of(new FieldViolation("request", "is required")));
        }

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
        return statusUpdateMapper.toStatusUpdateResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentStatusUpdateResponse markNoShow(UUID id) {
        if (id == null) {
            throw new ValidationException("Appointment id is required",
                List.of(new FieldViolation("id", "is required")));
        }

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
        return statusUpdateMapper.toStatusUpdateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatusAndDate(AppointmentStatus status, LocalDate date) {
        if (status == null) {
            throw new ValidationException("Status is required",
                List.of(new FieldViolation("status", "is required")));
        }
        if (date == null) {
            throw new ValidationException("Date is required",
                List.of(new FieldViolation("date", "is required")));
        }
        return appointmentRepository.countByStatusAndDate(status, date);
    }

}
