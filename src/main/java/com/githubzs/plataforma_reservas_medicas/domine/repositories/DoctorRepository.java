package com.githubzs.plataforma_reservas_medicas.domine.repositories;


import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    
    Page<Doctor> findByStatusAndSpecialty_Id(DoctorStatus status, UUID specialty, Pageable pageable);

    boolean existsByIdAndStatus(UUID id, DoctorStatus status);

    // Contar citas completadas por doctor en un rango de fechas
    @Query("""
        SELECT d.id, d.fullName, COUNT(a.id) AS completedCount
        FROM Doctor d
        JOIN d.appointments a
        WHERE a.status = 'COMPLETED'
        AND a.startAt BETWEEN :from AND :to
        GROUP BY d.id, d.fullName
        ORDER BY completedCount DESC
    """)
    Page<Object[]> rankDoctorsByCompletedAppointments(
        @Param("from") LocalDateTime from,
        @Param("to")   LocalDateTime to,
        Pageable pageable
    );

    @Query("""
    SELECT COUNT(a) > 0 FROM Appointment a
    WHERE a.doctor.id = :doctorId
    AND a.status NOT IN ('CANCELLED')
    AND a.startAt < :endAt
    AND a.endAt > :startAt
    """)
    boolean existsOverlapForDoctor(
    @Param("doctorId") UUID doctorId,
    @Param("startAt") LocalDateTime startAt,
    @Param("endAt") LocalDateTime endAt
    );


    // HU-07 (al editar — excluye la cita actual)
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.id <> :excludeId
        AND a.status NOT IN ('CANCELLED')
        AND a.startAt < :endAt
        AND a.endAt > :startAt
    """)
    boolean existsOverlapForDoctorExcluding(
        @Param("doctorId") UUID doctorId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt,
        @Param("excludeId") UUID excludeId
    );

}


