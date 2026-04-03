package com.githubzs.plataforma_reservas_medicas.domine.repositories;



import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

public interface OfficeRepository extends JpaRepository<Office, UUID> {

    boolean existsByIdAndStatus(UUID id, OfficeStatus status);

    Page<Office> findByStatus(OfficeStatus status, Pageable pageable);

     // HU-06 — slots ocupados del consultorio en una fecha (para calcular libres)
    @Query("""
    SELECT COUNT(a) > 0 FROM Appointment a
    WHERE a.office.id = :officeId
    AND a.status NOT IN ('CANCELLED')
    AND a.startAt < :endAt
    AND a.endAt > :startAt
    """)
    boolean existsOverlapForOffice(
        @Param("officeId") UUID officeId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );
    
}
