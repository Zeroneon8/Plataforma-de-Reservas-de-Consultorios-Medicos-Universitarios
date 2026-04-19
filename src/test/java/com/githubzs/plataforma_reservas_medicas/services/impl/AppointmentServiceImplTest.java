package com.githubzs.plataforma_reservas_medicas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.UUID;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
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
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.mapper.PatientSummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.SpecialtySummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentSummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentTypeSummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.DoctorSummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.mapper.OfficeSummaryMapperImpl;
import com.githubzs.plataforma_reservas_medicas.services.validator.AppointmentValidator;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentValidator validator;

    @InjectMocks
    private AppointmentServiceImpl service;

    private UUID patientId;
    private UUID doctorId;
    private UUID officeId;
    private UUID appointmentTypeId;
    private UUID appointmentId;
    private LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

    // Metodo para setear mappers de forma manual con sus dependencias, ya que @Spy no detecta estas dependencias
    @BeforeEach
    void setUp() throws Exception {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        officeId = UUID.randomUUID();
        appointmentTypeId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
    
        var patientSummaryMapperImpl = new PatientSummaryMapperImpl();
        var officeSummaryMapperImpl = new OfficeSummaryMapperImpl();
        var appointmentTypeSummaryMapperImpl = new AppointmentTypeSummaryMapperImpl();
        var specialtySummaryMapperImpl = new SpecialtySummaryMapperImpl();
        var doctorSummaryMapperImpl = new DoctorSummaryMapperImpl();
        var appointmentSummaryMapperImpl = new AppointmentSummaryMapperImpl();
        var appointmentMapperImpl = new AppointmentMapperImpl();
    
        setField(doctorSummaryMapperImpl, "specialtySummaryMapper", specialtySummaryMapperImpl);
    
        setField(appointmentSummaryMapperImpl, "patientSummaryMapper", patientSummaryMapperImpl);
        setField(appointmentSummaryMapperImpl, "doctorSummaryMapper", doctorSummaryMapperImpl);
    
        setField(appointmentMapperImpl, "patientSummaryMapper", patientSummaryMapperImpl);
        setField(appointmentMapperImpl, "doctorSummaryMapper", doctorSummaryMapperImpl);
        setField(appointmentMapperImpl, "officeSummaryMapper", officeSummaryMapperImpl);
        setField(appointmentMapperImpl, "appointmentTypeSummaryMapper", appointmentTypeSummaryMapperImpl);
    
        // overwrite the service fields created by @InjectMocks with our real mappers
        setField(service, "mapper", appointmentMapperImpl);
        setField(service, "summaryMapper", appointmentSummaryMapperImpl);
    }
    
    // Metodo helper para setear las dependencias de los mappers manualmente
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
    void shouldCreateAppointmentWhenAllValidationsPass() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(validator.validatePatientExistsAndActive(patientId)).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctorId)).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(officeId)).thenReturn(office);
        when(validator.validateAppointmentTypeExists(appointmentTypeId)).thenReturn(type);
        doNothing().when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateAppointmentWithinDoctorSchedule(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateNoOverlapForDoctor(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateNoOverlapForOffice(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateNoOverlapForPatient(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(appointmentId);
            return a;
        });

        var result = service.create(request);

        assertNotNull(result);
        verify(appointmentRepository).save(any(Appointment.class));
        assertEquals(appointmentId, result.id());
        assertEquals(patientId, result.patient().id());
        assertEquals(doctorId, result.doctor().id());
        assertEquals(officeId, result.office().id());
        assertEquals(appointmentTypeId, result.appointmentType().id());
        assertEquals(startAt, result.startAt());
        assertEquals(startAt.plusMinutes(type.getDurationMinutes()), result.endAt());
        assertEquals(AppointmentStatus.SCHEDULED, result.status());
        assertEquals(result.cancelReason(), null);
        assertEquals(result.observations(), null);
        assertNotNull(result.createdAt());
    }

    @Test
    void shouldThrowValidationExceptionWhenRequestIsNullForCreate() {
        assertThrows(ValidationException.class, () -> service.create(null));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDoctorDoesNotExistForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        when(validator.validatePatientExistsAndActive(patientId)).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctorId)).thenThrow(new ResourceNotFoundException("Doctor not found with id " + doctorId));

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        verify(validator).validatePatientExistsAndActive(patientId);
        verify(validator).validateDoctorExistsAndActive(doctorId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOfficeDoesNotExistForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        when(validator.validatePatientExistsAndActive(patientId)).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctorId)).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(officeId)).thenThrow(new ResourceNotFoundException("Office not found with id " + officeId));

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        verify(validator).validatePatientExistsAndActive(patientId);
        verify(validator).validateDoctorExistsAndActive(doctorId);
        verify(validator).validateOfficeExistsAndAvailable(officeId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAppointmentTypeDoesNotExistForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        when(validator.validatePatientExistsAndActive(patientId)).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctorId)).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(officeId)).thenReturn(office);
        when(validator.validateAppointmentTypeExists(appointmentTypeId)).thenThrow(new ResourceNotFoundException("Appointment type not found with id " + appointmentTypeId));

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(validator).validatePatientExistsAndActive(patientId);
        verify(validator).validateDoctorExistsAndActive(doctorId);
        verify(validator).validateOfficeExistsAndAvailable(officeId);
        verify(validator).validateAppointmentTypeExists(appointmentTypeId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPatientDoesNotExistForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        when(validator.validatePatientExistsAndActive(patientId)).thenThrow(new ResourceNotFoundException("Patient not found with id " + patientId));

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(validator).validatePatientExistsAndActive(patientId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenPatientInactiveForCreate() {
        var startAt = baseDateTime.plusDays(1);
        
        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        
        var patient = Patient.builder().id(patientId).status(PatientStatus.INACTIVE).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenThrow(new ConflictException("Patient is not active"));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validatePatientExistsAndActive(patientId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenDoctorInactiveForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        
        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.INACTIVE).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenThrow(new ConflictException("Doctor is not active"));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validatePatientExistsAndActive(patientId);
        verify(validator).validateDoctorExistsAndActive(doctorId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenOfficeUnavailableForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        
        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.MAINTENANCE).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenThrow(new ConflictException("Office is not available"));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validatePatientExistsAndActive(patientId);
        verify(validator).validateDoctorExistsAndActive(doctorId);
        verify(validator).validateOfficeExistsAndAvailable(officeId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenDateInPastForCreate() {
        var startAt = baseDateTime.minusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        
        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenReturn(office);
        when(validator.validateAppointmentTypeExists(type.getId())).thenReturn(type);
        doThrow(new ConflictException("Cannot create appointment in the past")).when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validatePatientExistsAndActive(patientId);
        verify(validator).validateDoctorExistsAndActive(doctorId);
        verify(validator).validateOfficeExistsAndAvailable(officeId);
        verify(validator).validateAppointmentTypeExists(appointmentTypeId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenStartAtIsAfterEndAtForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        
        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(-30).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenReturn(office);
        when(validator.validateAppointmentTypeExists(type.getId())).thenReturn(type);
        doThrow(new ConflictException("Appointment end time must be after start time")).when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validateAppointmentStartAtEndAt(any(), any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenAppointmentOutsideDoctorScheduleForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);
        
        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenReturn(office);
        when(validator.validateAppointmentTypeExists(type.getId())).thenReturn(type);
        doNothing().when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));
        doThrow(new ConflictException("Appointment time is outside of doctor's schedule")).when(validator).validateAppointmentWithinDoctorSchedule(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validateAppointmentWithinDoctorSchedule(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenDoctorHasOverlapForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenReturn(office);
        when(validator.validateAppointmentTypeExists(type.getId())).thenReturn(type);
        doNothing().when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateAppointmentWithinDoctorSchedule(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doThrow(new ConflictException("Doctor has another appointment during this time")).when(validator).validateNoOverlapForDoctor(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validateNoOverlapForDoctor(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenOfficeHasOverlapForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenReturn(office);
        when(validator.validateAppointmentTypeExists(type.getId())).thenReturn(type);
        doNothing().when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateAppointmentWithinDoctorSchedule(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateNoOverlapForDoctor(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doThrow(new ConflictException("Office has another appointment during this time")).when(validator).validateNoOverlapForOffice(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
    
        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validateNoOverlapForOffice(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenPatientHasOverlapForCreate() {
        var startAt = baseDateTime.plusDays(1);

        var request = new AppointmentCreateRequest(doctorId, patientId, officeId, appointmentTypeId, startAt);

        var patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();
        var doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();
        var office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        when(validator.validatePatientExistsAndActive(patient.getId())).thenReturn(patient);
        when(validator.validateDoctorExistsAndActive(doctor.getId())).thenReturn(doctor);
        when(validator.validateOfficeExistsAndAvailable(office.getId())).thenReturn(office);
        when(validator.validateAppointmentTypeExists(type.getId())).thenReturn(type);
        doNothing().when(validator).validateAppointmentStartAtEndAt(any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateAppointmentWithinDoctorSchedule(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateNoOverlapForDoctor(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doNothing().when(validator).validateNoOverlapForOffice(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        doThrow(new ConflictException("Patient has another appointment during this time")).when(validator).validateNoOverlapForPatient(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(validator).validateNoOverlapForPatient(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldChangeStatusToConfirmedForConfirm() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.SCHEDULED).build();

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(appointmentId);
            return a;
        });

        var result = service.confirm(appointmentId);

        assertEquals(AppointmentStatus.CONFIRMED, result.status());
        assertEquals(appointmentId, result.id());
        assertNotNull(result.updatedAt());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenIdIsNullForConfirm() {
        assertThrows(ValidationException.class, () -> service.confirm(null));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAppointmentDoesNotExistForConfirm() {
        when(validator.validateAppointmentExists(appointmentId)).thenThrow(new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        assertThrows(ResourceNotFoundException.class, () -> service.confirm(appointmentId));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenAppointmentNotScheduledForConfirm() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.CANCELLED).build();

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);

        assertThrows(ConflictException.class, () -> service.confirm(appointmentId));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldChangeStatusToCancelledForCancel() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.SCHEDULED).build();

        var request = new AppointmentCancelRequest("Medical issue");

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(appointmentId);
            return a;
        });

        var result = service.cancel(appointmentId, request);

        assertEquals(AppointmentStatus.CANCELLED, result.status());
        assertEquals(appointmentId, result.id());
        assertNotNull(result.updatedAt());
        assertEquals("Medical issue", result.cancelReason());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenIdIsNullForCancel() {
        var request = new AppointmentCancelRequest("Reason");
        assertThrows(ValidationException.class, () -> service.cancel(null, request));
    }

    @Test
    void shouldThrowValidationExceptionWhenRequestIsNullForCancel() {
        assertThrows(ValidationException.class, () -> service.cancel(appointmentId, null));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAppointmentDoesNotExistForCancel() {
        var request = new AppointmentCancelRequest("Reason");

        when(validator.validateAppointmentExists(appointmentId)).thenThrow(new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        assertThrows(ResourceNotFoundException.class, () -> service.cancel(appointmentId, request));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenAppointmentNotScheduledOrConfirmedForCancel() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.COMPLETED).build();

        var request = new AppointmentCancelRequest("Reason");

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);

        assertThrows(ConflictException.class, () -> service.cancel(appointmentId, request));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldChangeStatusToCompletedForComplete() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.CONFIRMED).startAt(baseDateTime).build();    

        var request = new AppointmentCompleteRequest("Good progress");

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(appointmentId);
            return a;
        });

        var result = service.complete(appointmentId, request);

        assertEquals(AppointmentStatus.COMPLETED, result.status());
        assertEquals(appointmentId, result.id());
        assertNotNull(result.updatedAt());
        assertEquals("Good progress", result.observations());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenIdIsNullForComplete() {
        var request = new AppointmentCompleteRequest("Observations");
        assertThrows(ValidationException.class, () -> service.complete(null, request));
    }

    @Test
    void shouldThrowValidationExceptionWhenRequestIsNullForComplete() {
        assertThrows(ValidationException.class, () -> service.complete(appointmentId, null));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAppointmentDoesNotExistForComplete() {
        var request = new AppointmentCompleteRequest("Observations");

        when(validator.validateAppointmentExists(appointmentId)).thenThrow(new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        assertThrows(ResourceNotFoundException.class, () -> service.complete(appointmentId, request));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenAppointmentNotConfirmedForComplete() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.SCHEDULED).build();
        var request = new AppointmentCompleteRequest("Observations");

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);

        assertThrows(ConflictException.class, () -> service.complete(appointmentId, request));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldMarkStatusAsNoShowForMarkNoShow() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.CONFIRMED).startAt(baseDateTime).build();

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(appointmentId);
            return a;
        });

        var result = service.markNoShow(appointmentId);

        assertEquals(AppointmentStatus.NO_SHOW, result.status());
        assertEquals(appointmentId, result.id());
        assertNotNull(result.updatedAt());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenIdIsNullForMarkNoShow() {
        assertThrows(ValidationException.class, () -> service.markNoShow(null));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAppointmentDoesNotExistForMarkNoShow() {
        when(validator.validateAppointmentExists(appointmentId)).thenThrow(new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        assertThrows(ResourceNotFoundException.class, () -> service.markNoShow(appointmentId));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenAppointmentNotConfirmedForMarkNoShow() {
        var appointment = Appointment.builder().id(appointmentId).status(AppointmentStatus.SCHEDULED).build();

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);

        assertThrows(ConflictException.class, () -> service.markNoShow(appointmentId));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenMarkingBeforeScheduledStartTimeForMarkNoShow() {
        var appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));

        when(validator.validateAppointmentExists(appointmentId)).thenReturn(appointment);

        assertThrows(ConflictException.class, () -> service.markNoShow(appointmentId));
        verify(validator).validateAppointmentExists(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

}
