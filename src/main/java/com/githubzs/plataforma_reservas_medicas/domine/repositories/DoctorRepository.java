package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.githubzs.plataforma_reservas_medicas.domine.dto.DoctorRankingStatsDto;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;


public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    
    Optional<Doctor> findByDocumentNumber(String documentNumber);

    Page<Doctor> findBySpecialty_Id(UUID specialtyId, Pageable pageable);

    Page<Doctor> findByStatusAndSpecialty_Id(DoctorStatus status, UUID specialtyId, Pageable pageable);

    boolean existsByIdAndStatus(UUID id, DoctorStatus status);

    // Obtener ranking de profesionales por cantidad de citas completadas (Ordenamos y no ponemos el ranking directamente porque JPQL no soporta funciones de ventana)
    @Query("""
     SELECT new com.githubzs.plataforma_reservas_medicas.domine.dto.DoctorRankingStatsDto(
      d.id,
      d.fullName,
      COUNT(a)
     )
     FROM Doctor d
     LEFT JOIN d.appointments a
     ON a.status = com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus.COMPLETED
     GROUP BY d.id, d.fullName
     ORDER BY COUNT(a) DESC
    """)
    List<DoctorRankingStatsDto> rankDoctorsByCompletedAppointments();

}


