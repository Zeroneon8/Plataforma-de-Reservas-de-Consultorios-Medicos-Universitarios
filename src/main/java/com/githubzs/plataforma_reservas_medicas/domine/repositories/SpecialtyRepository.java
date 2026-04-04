package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.githubzs.plataforma_reservas_medicas.domine.dto.SpecialtyAttendanceStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    boolean existsByName(String name);

    Optional<Specialty> findByName(String name);

    // Contar citas canceladas y no asistidas agrupadas por especialidad
    @Query("""
     Select new com.githubzs.plataforma_reservas_medicas.domine.dto.SpecialtyAttendanceStatsDto(
     s.id,
     s.name,
     COALESCE(SUM(CASE WHEN a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED THEN 1 ELSE 0 END), 0),
     COALESCE(SUM(CASE WHEN a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.NO_SHOW THEN 1 ELSE 0 END), 0)
     )
     FROM Specialty s
     LEFT JOIN s.doctors d
     LEFT JOIN d.appointments a
     GROUP BY s.id, s.name
     ORDER BY s.name
    """)
    List<SpecialtyAttendanceStatsDto> countCancelledAndNoShowBySpecialty();
    
}
