package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    Optional<Appointment> findByIdAndStatus(UUID id, AppointmentStatus status);


    // 6.1 — un paciente no puede tener dos citas activas que se crucen
     @Query("""
     SELECT COUNT(a) > 0 FROM Appointment a
     WHERE a.patient.id = :patientId
     AND a.status NOT IN ('CANCELLED')
     AND a.startAt < :endAt
     AND a.endAt > :startAt
    """)
    boolean existsOverlapForPatient(
        @Param("patientId") UUID patientId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );

}
