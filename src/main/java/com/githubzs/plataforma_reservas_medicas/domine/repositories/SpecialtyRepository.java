package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    boolean existsByName(String name);

    // HU-12 — citas canceladas y no asistidas agrupadas por especialidad
    @Query("""
        SELECT d.specialty.name, COUNT(a.id)
        FROM Appointment a
        JOIN a.doctor d
        WHERE a.status IN ('CANCELLED', 'NO_SHOW')
        AND a.startAt BETWEEN :from AND :to
        GROUP BY d.specialty.name
    """)
    List<Object[]> countCancelledAndNoShowBySpecialty(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
    
}
