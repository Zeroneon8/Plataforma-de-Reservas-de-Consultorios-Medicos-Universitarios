package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findByPatient_IdAndStatus(UUID patientId, AppointmentStatus status, Pageable pageable);
    
    Page<Appointment> findByStartAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Comprobar que un paciente no tenga citas activas que se crucen en el tiempo
     @Query("""
     SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END 
     FROM Appointment a
     WHERE a.patient.id = :patientId
     AND a.status <> com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED
     AND a.startAt < :endAt
     AND a.endAt > :startAt
    """)
    boolean existsOverlapForPatient(
        @Param("patientId") UUID patientId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );

    // Comprobar que un doctor no tenga citas activas que se crucen en el tiempo
    @Query("""
     SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END 
     FROM Appointment a
     WHERE a.doctor.id = :doctorId
     AND a.status <> com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED
     AND a.startAt < :endAt
     AND a.endAt > :startAt
    """)
    boolean existsOverlapForDoctor(
        @Param("doctorId") UUID doctorId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );

    // Comprobar que una oficina no tenga citas activas que se crucen en el tiempo
    @Query("""
     SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END 
     FROM Appointment a
     WHERE a.office.id = :officeId
     AND a.status <> com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED
     AND a.startAt < :endAt
     AND a.endAt > :startAt
    """)
    boolean existsOverlapForOffice(
        @Param("officeId") UUID officeId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );

    // Obtener citas de un doctor en un rango de fechas especifico
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.status <> com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED
        AND a.startAt >= :start
        AND a.startAt < :end
    """)
    List<Appointment> findAppointmentsByDoctorBetween(
        @Param("doctorId") UUID doctorId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
