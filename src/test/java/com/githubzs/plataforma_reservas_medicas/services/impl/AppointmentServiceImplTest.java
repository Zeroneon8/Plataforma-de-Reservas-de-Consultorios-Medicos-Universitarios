package com.githubzs.plataforma_reservas_medicas.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSearchRequest;
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
import com.githubzs.plataforma_reservas_medicas.services.mapper.AppointmentStatusUpdateMapperImpl;
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
    private String doctorDocumentNumber;
    private UUID officeId;
    private UUID appointmentTypeId;
    private UUID appointmentId;
    private LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);

    // Metodo para setear mappers de forma manual con sus dependencias, ya que @Spy no detecta estas dependencias
    @BeforeEach
    void setUp() throws Exception {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        doctorDocumentNumber = "123456789";
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
        var appointmentStatusUpdateMapperImpl = new AppointmentStatusUpdateMapperImpl();
    
        setField(doctorSummaryMapperImpl, "specialtySummaryMapper", specialtySummaryMapperImpl);
    
        setField(appointmentSummaryMapperImpl, "patientSummaryMapper", patientSummaryMapperImpl);
    
        setField(appointmentMapperImpl, "patientSummaryMapper", patientSummaryMapperImpl);
        setField(appointmentMapperImpl, "doctorSummaryMapper", doctorSummaryMapperImpl);
        setField(appointmentMapperImpl, "officeSummaryMapper", officeSummaryMapperImpl);
        setField(appointmentMapperImpl, "appointmentTypeSummaryMapper", appointmentTypeSummaryMapperImpl);
    
        // overwrite the service fields created by @InjectMocks with our real mappers
        setField(service, "mapper", appointmentMapperImpl);
        setField(service, "summaryMapper", appointmentSummaryMapperImpl);
        setField(service, "statusUpdateMapper", appointmentStatusUpdateMapperImpl);
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
    void shouldFindByDoctorDocumentNumberWhenValid() {
        var pageable = PageRequest.of(0, 5);
        var doctor = Doctor.builder().id(doctorId).documentNumber(doctorDocumentNumber).status(DoctorStatus.ACTIVE).build();
        var patient = Patient.builder().id(patientId).build();

        var appointment1 = Appointment.builder()
            .id(UUID.randomUUID())
            .doctor(doctor)
            .patient(patient)
            .status(AppointmentStatus.CONFIRMED)
            .startAt(baseDateTime)
            .endAt(baseDateTime.plusMinutes(30))
            .build();
        var appointment2 = Appointment.builder()
            .id(UUID.randomUUID())
            .doctor(doctor)
            .patient(patient)
            .status(AppointmentStatus.SCHEDULED)
            .startAt(baseDateTime.plusHours(1))
            .endAt(baseDateTime.plusHours(1).plusMinutes(30))
            .build();

        when(validator.validateDoctorExistsAndActiveByDocumentNumber(doctorDocumentNumber)).thenReturn(doctor);
        when(appointmentRepository.findByDoctor_DocumentNumber(doctorDocumentNumber, pageable))
            .thenReturn(new PageImpl<>(List.of(appointment1, appointment2), pageable, 2));

        var result = service.findByDoctorDocumentNumber(doctorDocumentNumber, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(appointment1.getId(), result.getContent().get(0).id());
        assertEquals(appointment2.getId(), result.getContent().get(1).id());
        verify(validator).validateDoctorExistsAndActiveByDocumentNumber(doctorDocumentNumber);
        verify(appointmentRepository).findByDoctor_DocumentNumber(doctorDocumentNumber, pageable);
    }

    @Test
    void shouldUseDefaultPageableWhenNullForFindByDoctorDocumentNumber() {
        var doctor = Doctor.builder().id(doctorId).documentNumber(doctorDocumentNumber).status(DoctorStatus.ACTIVE).build();

        when(validator.validateDoctorExistsAndActiveByDocumentNumber(doctorDocumentNumber)).thenReturn(doctor);
        when(appointmentRepository.findByDoctor_DocumentNumber(eq(doctorDocumentNumber), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var result = service.findByDoctorDocumentNumber(doctorDocumentNumber, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentRepository).findByDoctor_DocumentNumber(eq(doctorDocumentNumber), pageableCaptor.capture());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void shouldThrowValidationExceptionWhenDoctorDocumentNumberIsNullForFindByDoctorDocumentNumber() {
        assertThrows(ValidationException.class, () -> service.findByDoctorDocumentNumber(null, PageRequest.of(0, 5)));
        verify(validator, never()).validateDoctorExistsAndActiveByDocumentNumber(any());
        verify(appointmentRepository, never()).findByDoctor_DocumentNumber(any(), any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDoctorDoesNotExistForFindByDoctorDocumentNumber() {
        when(validator.validateDoctorExistsAndActiveByDocumentNumber(doctorDocumentNumber))
            .thenThrow(new ResourceNotFoundException("Doctor not found with document number " + doctorDocumentNumber));

        assertThrows(ResourceNotFoundException.class, () -> service.findByDoctorDocumentNumber(doctorDocumentNumber, PageRequest.of(0, 5)));
        verify(validator).validateDoctorExistsAndActiveByDocumentNumber(doctorDocumentNumber);
        verify(appointmentRepository, never()).findByDoctor_DocumentNumber(any(), any());
    }

    @Test
    void shouldFindAllWithFiltersWhenValidRequest() {
        var pageable = PageRequest.of(0, 10);
        var request = new AppointmentSearchRequest(patientId, doctorId, officeId, null, baseDateTime, baseDateTime.plusHours(2), AppointmentStatus.CONFIRMED);

        var patient = Patient.builder().id(patientId).build();
        var doctor = Doctor.builder().id(doctorId).build();
        var office = Office.builder().id(officeId).build();
        var type = AppointmentType.builder().id(appointmentTypeId).durationMinutes(30).build();

        var appointment = Appointment.builder()
            .id(appointmentId)
            .patient(patient)
            .doctor(doctor)
            .office(office)
            .appointmentType(type)
            .startAt(baseDateTime)
            .endAt(baseDateTime.plusMinutes(30))
            .status(AppointmentStatus.CONFIRMED)
            .build();

        when(appointmentRepository.findAll(ArgumentMatchers.<Specification<Appointment>>any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(appointment), pageable, 1));

        var result = service.findAll(request, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(appointmentId, result.getContent().get(0).id());
        assertEquals(patientId, result.getContent().get(0).patient().id());
        assertEquals(doctorId, result.getContent().get(0).doctor().id());
        verify(appointmentRepository).findAll(ArgumentMatchers.<Specification<Appointment>>any(), eq(pageable));
    }

    @Test
    void shouldUseDefaultPageableWhenNullForFindAll() {
        when(appointmentRepository.findAll(ArgumentMatchers.<Specification<Appointment>>any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var result = service.findAll(null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentRepository).findAll(ArgumentMatchers.<Specification<Appointment>>any(), pageableCaptor.capture());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void shouldThrowValidationExceptionWhenStartAtAfterEndAtForFindAll() {
        var request = new AppointmentSearchRequest(null, null, null, null, baseDateTime.plusHours(2), baseDateTime, null);

        assertThrows(ValidationException.class, () -> service.findAll(request, PageRequest.of(0, 10)));
        verify(appointmentRepository, never()).findAll(ArgumentMatchers.<Specification<Appointment>>any(), any(Pageable.class));
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

    @Test
    void shouldCountByStatusAndDateWhenValid() {
        var date = LocalDate.of(2026, 3, 5);
        var status = AppointmentStatus.SCHEDULED;

        when(appointmentRepository.countByStatusAndDate(status, date)).thenReturn(4L);

        var result = service.countByStatusAndDate(status, date);

        assertEquals(4L, result);
        verify(appointmentRepository).countByStatusAndDate(AppointmentStatus.SCHEDULED, date);
    }

    @Test
    void shouldThrowValidationExceptionWhenStatusIsNullForCountByStatusAndDate() {
        var date = LocalDate.of(2026, 3, 5);

        assertThrows(ValidationException.class, () -> service.countByStatusAndDate(null, date));
        verify(appointmentRepository, never()).countByStatusAndDate(any(), any());
    }

    @Test
    void shouldThrowValidationExceptionWhenDateIsNullForCountByStatusAndDate() {
        var status = AppointmentStatus.SCHEDULED;

        assertThrows(ValidationException.class, () -> service.countByStatusAndDate(status, null));
        verify(appointmentRepository, never()).countByStatusAndDate(any(), any());
    }

    @Test
    void shouldCountByDateWhenValid() {
        var date = LocalDate.of(2026, 3, 5);

        when(appointmentRepository.countByDate(date)).thenReturn(3L);

        var result = service.countByDate(date);

        assertEquals(3L, result);
        verify(appointmentRepository).countByDate(date);
    }

    @Test
    void shouldThrowValidationExceptionWhenDateIsNullForCountByDate() {
        assertThrows(ValidationException.class, () -> service.countByDate(null));
        verify(appointmentRepository, never()).countByDate(any());
    }

}
