package com.githubzs.plataforma_reservas_medicas.services.validator;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.PatientRepository;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.DoctorRepository;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentTypeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.OfficeRepository;
import com.githubzs.plataforma_reservas_medicas.domine.repositories.AppointmentRepository;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ConflictException;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.services.impl.DoctorScheduleServiceImpl;
import com.githubzs.plataforma_reservas_medicas.api.error.ErrorResponse.FieldViolation;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final DoctorScheduleServiceImpl doctorScheduleServiceImpl;

    public Appointment validateAppointmentExists(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));
    }

    public Patient validatePatientExistsAndActive(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new ConflictException("Patient is not active");
        }

        return patient;
    }

    public Doctor validateDoctorExistsAndActive(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));
        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new ConflictException("Doctor is not active");
        }

        return doctor;
    }

    public Office validateOfficeExistsAndAvailable(UUID officeId) {
        Office office = officeRepository.findById(officeId)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id " + officeId));
        if (office.getStatus() != OfficeStatus.AVAILABLE) {
            throw new ConflictException("Office is not available");
        }

        return office;
    }

    public void validateAppointmentStartAtEndAt(LocalDateTime startAt, LocalDateTime endAt) {
        List<FieldViolation> violations = new ArrayList<>();
        if (startAt == null) {
            violations.add(new FieldViolation("startAt", "Start time is required"));
        }
        else if (startAt.isBefore(LocalDateTime.now())) {
            violations.add(new FieldViolation("startAt", "Start time must be in the future"));
        }

        if (endAt == null) {
            violations.add(new FieldViolation("endAt", "End time is required"));
        }
        else if (startAt != null && !endAt.isAfter(startAt)) {
            violations.add(new FieldViolation("endAt", "End time must be after start time"));
        }

        if (!violations.isEmpty()) {
            throw new ValidationException("Invalid appointment date times", violations);
        }
    }

    public AppointmentType validateAppointmentTypeExists(UUID appointmentTypeId) {
        return appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment type not found with id " + appointmentTypeId));
    }

    public void validateAppointmentWithinDoctorSchedule(UUID doctorId, LocalDateTime startAt, LocalDateTime endAt) {
        if (!doctorScheduleServiceImpl.isWithinSchedule(doctorId, startAt, endAt)) {
            throw new ConflictException("Appointment time is outside of doctor's schedule");
        }
    }

    public void validateNoOverlapForDoctor(UUID doctorId, LocalDateTime startAt, LocalDateTime endAt) {
        if (appointmentRepository.existsOverlapForDoctor(doctorId, startAt, endAt)) {
            throw new ConflictException("Doctor has another appointment during this time");
        }
    }

    public void validateNoOverlapForOffice(UUID officeId, LocalDateTime startAt, LocalDateTime endAt) {
        if (appointmentRepository.existsOverlapForOffice(officeId, startAt, endAt)) {
            throw new ConflictException("Office has another appointment during this time");
        }
    }

    public void validateNoOverlapForPatient(UUID patientId, LocalDateTime startAt, LocalDateTime endAt) {
        if (appointmentRepository.existsOverlapForPatient(patientId, startAt, endAt)) {
            throw new ConflictException("Patient has another appointment during this time");
        }
    }

}
