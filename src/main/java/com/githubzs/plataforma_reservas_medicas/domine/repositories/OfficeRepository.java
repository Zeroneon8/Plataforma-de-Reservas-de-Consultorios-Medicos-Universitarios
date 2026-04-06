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

    boolean existsByName(String name);

    boolean existsByRoomNumber(int roomNumber);

    Page<Office> findByStatus(OfficeStatus status, Pageable pageable);

    // Calcular ocupación de las oficinas en un rango de fechas (muestra tanto la cantidad de citas como la duración total de las mismas, y la cantidad de no-shows)
    @Query("""
     SELECT new com.githubzs.plataforma_reservas_medicas.domine.dto.OfficeOccupancyDto(
      o.id,
      o.name,
      o.location,
      o.roomNumber,
      COUNT(a),
      COALESCE(SUM(at.durationMinutes), 0),
      COALESCE(SUM(CASE WHEN a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.NO_SHOW THEN 1 ELSE 0 END), 0)
     )
     FROM Office o
     LEFT JOIN o.appointments a
     ON a.endAt > :from
     AND a.startAt < :to
     AND a.status <> com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.CANCELLED
     LEFT JOIN a.appointmentType at
     GROUP BY o.id, o.name, o.location, o.roomNumber
     ORDER BY COUNT(a) DESC
    """)
    List<OfficeOccupancyDto> calculateOfficeOccupancyBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
    
}
