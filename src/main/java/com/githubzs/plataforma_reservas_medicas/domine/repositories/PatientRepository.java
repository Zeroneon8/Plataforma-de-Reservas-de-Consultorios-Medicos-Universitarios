package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.domine.dto.PatientNoShowStatsDto;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsByIdAndStatus(UUID id, PatientStatus status);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentCodeIgnoreCase(String studentCode);

    Optional<Patient> findByDocumentNumber(String documentNumber);

    // Contar cantidad de inasistencias (NO_SHOW) de un paciente en un rango de fechas
    @Query("""
     SELECT COUNT(a) FROM Appointment a
     WHERE a.patient.id = :patientId
     AND a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.NO_SHOW
     AND a.startAt BETWEEN :from AND :to
    """)
    long countNoShowByPatient(
        @Param("patientId") UUID patientId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
    
    // Listar pacientes ordenados por cantidad de inasistencias (NO_SHOW) en un rango de fechas
    @Query("""
     SELECT new com.githubzs.plataforma_reservas_medicas.domine.dto.PatientNoShowStatsDto(
      a.patient.id,
      a.patient.fullName,
      COUNT(a)
     )
     FROM Appointment a
     WHERE a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.NO_SHOW
     AND a.startAt BETWEEN :from AND :to
     GROUP BY a.patient.id, a.patient.fullName
     ORDER BY COUNT(a) DESC, a.patient.fullName ASC
    """)
    List<PatientNoShowStatsDto> countPatientsNoShow(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
        
}
