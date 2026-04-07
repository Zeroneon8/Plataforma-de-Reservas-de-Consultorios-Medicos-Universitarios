package com.githubzs.plataforma_reservas_medicas.service.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
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
import com.githubzs.plataforma_reservas_medicas.service.impl.DoctorScheduleServiceImpl;

@ExtendWith(MockitoExtension.class)
class AppointmentValidatorTest {

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
	private DoctorScheduleServiceImpl doctorScheduleServiceImpl;

	@InjectMocks
	private AppointmentValidator validator;

	@Test
	void shouldReturnAppointmentWhenAppointmentExists() {
		UUID appointmentId = UUID.randomUUID();
		Appointment appointment = Appointment.builder().id(appointmentId).build();

		when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

		Appointment result = validator.validateAppointmentExists(appointmentId);

		assertSame(appointment, result);
		verify(appointmentRepository).findById(appointmentId);
	}

	@Test
	void shouldThrowNotFoundWhenAppointmentDoesNotExist() {
		UUID appointmentId = UUID.randomUUID();

		when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> validator.validateAppointmentExists(appointmentId));

		assertEquals("Appointment not found with id " + appointmentId, ex.getMessage());
	}

	@Test
	void shouldReturnPatientWhenPatientExistsAndIsActive() {
		UUID patientId = UUID.randomUUID();
		Patient patient = Patient.builder().id(patientId).status(PatientStatus.ACTIVE).build();

		when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

		Patient result = validator.validatePatientExistsAndActive(patientId);

		assertSame(patient, result);
		verify(patientRepository).findById(patientId);
	}

	@Test
	void shouldThrowNotFoundWhenPatientDoesNotExist() {
		UUID patientId = UUID.randomUUID();

		when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> validator.validatePatientExistsAndActive(patientId));

		assertEquals("Patient not found with id " + patientId, ex.getMessage());
	}

	@Test
	void shouldThrowConflictWhenPatientIsNotActive() {
		UUID patientId = UUID.randomUUID();
		Patient patient = Patient.builder().id(patientId).status(PatientStatus.INACTIVE).build();

		when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validatePatientExistsAndActive(patientId));

		assertEquals("Patient is not active", ex.getMessage());
	}

	@Test
	void shouldReturnDoctorWhenDoctorExistsAndIsActive() {
		UUID doctorId = UUID.randomUUID();
		Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.ACTIVE).build();

		when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

		Doctor result = validator.validateDoctorExistsAndActive(doctorId);

		assertSame(doctor, result);
		verify(doctorRepository).findById(doctorId);
	}

	@Test
	void shouldThrowNotFoundWhenDoctorDoesNotExist() {
		UUID doctorId = UUID.randomUUID();

		when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> validator.validateDoctorExistsAndActive(doctorId));

		assertEquals("Doctor not found with id " + doctorId, ex.getMessage());
	}

	@Test
	void shouldThrowConflictWhenDoctorIsNotActive() {
		UUID doctorId = UUID.randomUUID();
		Doctor doctor = Doctor.builder().id(doctorId).status(DoctorStatus.INACTIVE).build();

		when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateDoctorExistsAndActive(doctorId));

		assertEquals("Doctor is not active", ex.getMessage());
	}

	@Test
	void shouldReturnOfficeWhenOfficeExistsAndIsAvailable() {
		UUID officeId = UUID.randomUUID();
		Office office = Office.builder().id(officeId).status(OfficeStatus.AVAILABLE).build();

		when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));

		Office result = validator.validateOfficeExistsAndAvailable(officeId);

		assertSame(office, result);
		verify(officeRepository).findById(officeId);
	}

	@Test
	void shouldThrowNotFoundWhenOfficeDoesNotExist() {
		UUID officeId = UUID.randomUUID();

		when(officeRepository.findById(officeId)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> validator.validateOfficeExistsAndAvailable(officeId));

		assertEquals("Office not found with id " + officeId, ex.getMessage());
	}

	@Test
	void shouldThrowConflictWhenOfficeIsNotAvailable() {
		UUID officeId = UUID.randomUUID();
		Office office = Office.builder().id(officeId).status(OfficeStatus.MAINTENANCE).build();

		when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateOfficeExistsAndAvailable(officeId));

		assertEquals("Office is not available", ex.getMessage());
	}

	@Test
	void shouldNotThrowWhenStartAtAndEndAtAreValid() {
		LocalDateTime startAt = LocalDateTime.now().plusMinutes(20);
		LocalDateTime endAt = startAt.plusMinutes(30);

		assertDoesNotThrow(() -> validator.validateAppointmentStartAtEndAt(startAt, endAt));
	}

	@Test
	void shouldThrowConflictWhenStartAtIsInPast() {
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.minusMinutes(1);
		LocalDateTime endAt = baseDateTime.plusMinutes(20);

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateAppointmentStartAtEndAt(startAt, endAt));

		assertEquals("Cannot create appointment in the past", ex.getMessage());
	}

	@Test
	void shouldThrowConflictWhenEndAtIsNotAfterStartAt() {
        LocalDateTime startAt = LocalDateTime.now().plusMinutes(20);
		LocalDateTime endAt = startAt;

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateAppointmentStartAtEndAt(startAt, endAt));

		assertEquals("Appointment end time must be after start time", ex.getMessage());
	}

	@Test
	void shouldReturnAppointmentTypeWhenExists() {
		UUID appointmentTypeId = UUID.randomUUID();
		AppointmentType appointmentType = AppointmentType.builder().id(appointmentTypeId).build();

		when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));

		AppointmentType result = validator.validateAppointmentTypeExists(appointmentTypeId);

		assertSame(appointmentType, result);
		verify(appointmentTypeRepository).findById(appointmentTypeId);
	}

	@Test
	void shouldThrowNotFoundWhenAppointmentTypeDoesNotExist() {
		UUID appointmentTypeId = UUID.randomUUID();

		when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> validator.validateAppointmentTypeExists(appointmentTypeId));

		assertEquals("Appointment type not found with id " + appointmentTypeId, ex.getMessage());
	}

	@Test
	void shouldNotThrowWhenAppointmentIsWithinDoctorSchedule() {
		UUID doctorId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(doctorScheduleServiceImpl.isWithinSchedule(doctorId, startAt, endAt)).thenReturn(true);

		assertDoesNotThrow(() -> validator.validateAppointmentWithinDoctorSchedule(doctorId, startAt, endAt));
		verify(doctorScheduleServiceImpl).isWithinSchedule(doctorId, startAt, endAt);
	}

	@Test
	void shouldThrowConflictWhenAppointmentIsOutsideDoctorSchedule() {
		UUID doctorId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(doctorScheduleServiceImpl.isWithinSchedule(doctorId, startAt, endAt)).thenReturn(false);

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateAppointmentWithinDoctorSchedule(doctorId, startAt, endAt));

		assertEquals("Appointment time is outside of doctor's schedule", ex.getMessage());
	}

	@Test
	void shouldNotThrowWhenDoctorHasNoOverlap() {
		UUID doctorId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(appointmentRepository.existsOverlapForDoctor(doctorId, startAt, endAt)).thenReturn(false);

		assertDoesNotThrow(() -> validator.validateNoOverlapForDoctor(doctorId, startAt, endAt));
		verify(appointmentRepository).existsOverlapForDoctor(doctorId, startAt, endAt);
	}

	@Test
	void shouldThrowConflictWhenDoctorHasOverlap() {
		UUID doctorId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(appointmentRepository.existsOverlapForDoctor(doctorId, startAt, endAt)).thenReturn(true);

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateNoOverlapForDoctor(doctorId, startAt, endAt));

		assertEquals("Doctor has another appointment during this time", ex.getMessage());
	}

	@Test
	void shouldNotThrowWhenOfficeHasNoOverlap() {
		UUID officeId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(appointmentRepository.existsOverlapForOffice(officeId, startAt, endAt)).thenReturn(false);

		assertDoesNotThrow(() -> validator.validateNoOverlapForOffice(officeId, startAt, endAt));
		verify(appointmentRepository).existsOverlapForOffice(officeId, startAt, endAt);
	}

	@Test
	void shouldThrowConflictWhenOfficeHasOverlap() {
		UUID officeId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(appointmentRepository.existsOverlapForOffice(officeId, startAt, endAt)).thenReturn(true);

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateNoOverlapForOffice(officeId, startAt, endAt));

		assertEquals("Office has another appointment during this time", ex.getMessage());
	}

	@Test
	void shouldNotThrowWhenPatientHasNoOverlap() {
		UUID patientId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(appointmentRepository.existsOverlapForPatient(patientId, startAt, endAt)).thenReturn(false);

		assertDoesNotThrow(() -> validator.validateNoOverlapForPatient(patientId, startAt, endAt));
		verify(appointmentRepository).existsOverlapForPatient(patientId, startAt, endAt);
	}

	@Test
	void shouldThrowConflictWhenPatientHasOverlap() {
		UUID patientId = UUID.randomUUID();
        LocalDateTime baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
		LocalDateTime startAt = baseDateTime.plusDays(1);
		LocalDateTime endAt = startAt.plusMinutes(30);

		when(appointmentRepository.existsOverlapForPatient(patientId, startAt, endAt)).thenReturn(true);

		ConflictException ex = assertThrows(ConflictException.class,
				() -> validator.validateNoOverlapForPatient(patientId, startAt, endAt));

		assertEquals("Patient has another appointment during this time", ex.getMessage());
	}
}
