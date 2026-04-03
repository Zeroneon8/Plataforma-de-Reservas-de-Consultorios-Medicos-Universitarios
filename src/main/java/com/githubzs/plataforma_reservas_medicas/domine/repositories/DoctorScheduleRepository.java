package com.githubzs.plataforma_reservas_medicas.domine.repositories;


import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;




public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    List<DoctorSchedule> findByDoctorIdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);

    // HU-06 — slots ocupados del doctor en una fecha (para calcular libres)
    @Query("""
        SELECT a.startAt, a.endAt FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.status NOT IN ('CANCELLED')
        AND a.startAt >= :startOfDay
        AND a.endAt <= :endOfDay
        ORDER BY a.startAt ASC
    """)
    List<Object[]> findOccupiedSlotsByDoctorAndDate(
        @Param("doctorId") UUID doctorId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
    
}
