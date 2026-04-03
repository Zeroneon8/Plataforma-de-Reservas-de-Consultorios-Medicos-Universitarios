package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.dto.OfficeOccupancyDto;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

public interface OfficeRepository extends JpaRepository<Office, UUID> {

    boolean existsByIdAndStatus(UUID id, OfficeStatus status);

    Page<Office> findByStatus(OfficeStatus status, Pageable pageable);

    // Calcular ocupación de las oficinas en un rango de fechas (muestra tanto la cantidad de veces ocupada como la cantidad de minutos ocupada)
    @Query("""
     SELECT new com.githubzs.plataforma_reservas_medicas.domine.dto.OfficeOccupancyDto(
      o.id,
      COUNT(a),
      COALESCE(SUM(a.appointmentType.durationMinutes), 0),
      COALESCE(SUM(CASE WHEN a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.NO_SHOW THEN 1 ELSE 0 END), 0)
     )
     FROM Office o
     LEFT JOIN o.appointments a
     ON a.startAt >= :start
     AND a.startAt < :end
     AND a.status <> com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED
     GROUP BY o.id
     ORDER BY COUNT(a) DESC
    """)
    List<OfficeOccupancyDto> calculateOfficeOccupancyBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
}
