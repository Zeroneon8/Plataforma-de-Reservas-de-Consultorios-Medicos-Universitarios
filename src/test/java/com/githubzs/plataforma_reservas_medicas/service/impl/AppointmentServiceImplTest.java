package com.githubzs.plataforma_reservas_medicas.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
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
import com.githubzs.plataforma_reservas_medicas.service.DoctorScheduleService;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentMapper;
import com.githubzs.plataforma_reservas_medicas.service.mapper.AppointmentSummaryMapper;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;

    @Mock
    private DoctorScheduleService doctorScheduleService;

    @Mock
    private AppointmentMapper mapper;

    @Mock
    private AppointmentSummaryMapper summaryMapper;

    @InjectMocks
    private AppointmentServiceImpl service;

    private UUID patientId;
    private UUID doctorId;
    private UUID officeId;
    private UUID appointmentTypeId;
    private UUID appointmentId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        officeId = UUID.randomUUID();
        appointmentTypeId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
    }

    @Test
    void createShouldCreateAppointmentWhenAllValidationsPass() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endAt = startAt.plusMinutes(30);

        AppointmentCreateRequest request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        Patient patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        Office office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        Appointment entity = new Appointment();
        Appointment saved = Appointment.builder()
                .id(appointmentId)
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(type)
                .startAt(startAt)
                .endAt(endAt)
                .status(AppointmentStatus.SCHEDULED)
                .createdAt(Instant.now())
                .build();

        AppointmentResponse response = new AppointmentResponse(
                appointmentId, null, null, null, null, startAt, endAt, AppointmentStatus.SCHEDULED, null, null, null, null);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorScheduleService.isWithinSchedule(doctorId, startAt, endAt)).thenReturn(true);
        when(appointmentRepository.existsOverlapForDoctor(doctorId, startAt, endAt)).thenReturn(false);
        when(appointmentRepository.existsOverlapForOffice(officeId, startAt, endAt)).thenReturn(false);
        when(appointmentRepository.existsOverlapForPatient(patientId, startAt, endAt)).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        AppointmentResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(appointmentId, result.id());
        assertEquals(AppointmentStatus.SCHEDULED, result.status());
    }

    @Test
    void createShouldThrowConflictWhenPatientInactive() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        AppointmentCreateRequest request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        Patient patient = Patient.builder().id(patientId).status(PatientStatus.INACTIVE).build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void createShouldThrowConflictWhenDoctorInactive() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        AppointmentCreateRequest request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        Patient patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.INACTIVE).build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void createShouldThrowConflictWhenOfficeUnavailable() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        AppointmentCreateRequest request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        Patient patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        Office office = Office.builder().id(officeId).status(OfficeStatus.MAINTENANCE).build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void createShouldThrowConflictWhenDateInPast() {
        LocalDateTime startAt = LocalDateTime.now().minusHours(1);
        AppointmentCreateRequest request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        Patient patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        Office office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void createShouldThrowConflictWhenDoctorHasOverlap() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endAt = startAt.plusMinutes(30);
        AppointmentCreateRequest request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        Patient patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        Office office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        AppointmentType type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(type));
        when(doctorScheduleService.isWithinSchedule(doctorId, startAt, endAt)).thenReturn(true);
        when(appointmentRepository.existsOverlapForDoctor(doctorId, startAt, endAt)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void confirmShouldChangeStatusToConfirmed() {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment updated = new Appointment();
        updated.setId(appointmentId);
        updated.setStatus(AppointmentStatus.CONFIRMED);
        updated.setUpdatedAt(Instant.now());

        AppointmentSummaryResponse response = new AppointmentSummaryResponse(appointmentId, null, null, null, null, AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(updated);
        when(summaryMapper.toSummaryResponse(updated)).thenReturn(response);

        AppointmentSummaryResponse result = service.confirm(appointmentId);

        assertEquals(AppointmentStatus.CONFIRMED, result.status());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void cancelShouldChangeStatusToCancelled() {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        AppointmentCancelRequest request = new AppointmentCancelRequest("Medical issue");

        Appointment updated = new Appointment();
        updated.setId(appointmentId);
        updated.setStatus(AppointmentStatus.CANCELLED);
        updated.setCancelReason("Medical issue");
        updated.setUpdatedAt(Instant.now());

        AppointmentResponse response = new AppointmentResponse(appointmentId, null, null, null, null, null, null, AppointmentStatus.CANCELLED, "Medical issue", null, null, null);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        AppointmentResponse result = service.cancel(appointmentId, request);

        assertEquals(AppointmentStatus.CANCELLED, result.status());
        assertEquals("Medical issue", result.cancelReason());
    }

}
