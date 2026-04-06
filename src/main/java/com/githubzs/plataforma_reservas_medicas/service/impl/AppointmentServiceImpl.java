package com.githubzs.plataforma_reservas_medicas.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequestDto;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.service.AppointmentService;
import com.githubzs.plataforma_reservas_medicas.service.DoctorScheduleService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentSummaryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final DoctorScheduleService doctorScheduleService;
    private final AppointmentMapper mapper;
    private final AppointmentSummaryMapper summaryMapper;

    @Override
    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        Objects.requireNonNull(request, "Appointment create request is required");

        // Validar que el paciente existe y está activo
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.patientId()));
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new ConflictException("Patient is not active");
        }

        // Validar que el doctor existe y está activo
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + request.doctorId()));
        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new ConflictException("Doctor is not active");
        }

        // Validar que el consultorio existe y está disponible
        Office office = officeRepository.findById(request.officeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id " + request.officeId()));
        if (office.getStatus() != OfficeStatus.AVAILABLE) {
            throw new ConflictException("Office is not available");
        }

        // Validar que el tipo de cita existe
        AppointmentType appointmentType = appointmentTypeRepository.findById(request.appointmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + request.appointmentTypeId()));

        // Validar que la fecha/hora no es pasada
        LocalDateTime now = LocalDateTime.now();
        if (request.startAt().isBefore(now) || request.startAt().equals(now)) {
            throw new ConflictException("Cannot create appointment in the past");
        }

        // Validar que la cita cae dentro del horario laboral del doctor
        LocalDateTime endAt = request.startAt().plusMinutes(appointmentType.getDurationMinutes());
        if (!doctorScheduleService.isWithinSchedule(request.doctorId(), request.startAt(), endAt)) {
            throw new ConflictException("Appointment is outside doctor's working hours");
        }

        // Validar que no existe traslape para el doctor
        if (appointmentRepository.existsOverlapForDoctor(request.doctorId(), request.startAt(), endAt)) {
            throw new ConflictException("Doctor has overlapping appointment");
        }

        // Validar que no existe traslape para el consultorio
        if (appointmentRepository.existsOverlapForOffice(request.officeId(), request.startAt(), endAt)) {
            throw new ConflictException("Office has overlapping appointment");
        }

        // Validar que el paciente no tiene dos citas activas que se crucen en el tiempo
        if (appointmentRepository.existsOverlapForPatient(request.patientId(), request.startAt(), endAt)) {
            throw new ConflictException("Patient has overlapping appointment");
        }

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
        return appointmentRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryResponse> findAll(
            UUID patientId,
            UUID doctorId,
            AppointmentStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {

        LocalDateTime fromDateTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime toDateTime   = dateTo   != null ? dateTo.atTime(LocalTime.MAX) : null;

        return appointmentRepository
                .findAllWithFilters(patientId, doctorId, status, fromDateTime, toDateTime)
                .stream()
                .map(summaryMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentSummaryResponse confirm(UUID id) {
        Objects.requireNonNull(id, "Appointment id is required");

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

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
    public AppointmentResponse cancel(UUID id, AppointmentCancelRequest request) {
        Objects.requireNonNull(id, "Appointment id is required");
        Objects.requireNonNull(request, "Cancel request is required");

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

        // Solo se pueden cancelar citas en estado SCHEDULED o CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ConflictException("Only SCHEDULED or CONFIRMED appointments can be cancelled");
        }

        // La cancelación debe registrar un motivo obligatorio
        appointment.setCancelReason(request.cancelReason());
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse complete(UUID id, AppointmentCompleteRequestDto request) {
        Objects.requireNonNull(id, "Appointment id is required");
        Objects.requireNonNull(request, "Complete request is required");

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

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
        appointment.setObservations(request.observations());
        appointment.setUpdatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentSummaryResponse markNoShow(UUID id) {
        Objects.requireNonNull(id, "Appointment id is required");

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

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
