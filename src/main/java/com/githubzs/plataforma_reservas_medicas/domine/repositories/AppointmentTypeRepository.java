package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, UUID> {

    // HU-04 — verificar que existe al crear cita
    boolean existsById(UUID id);

    Optional<AppointmentType> findByIdAndStatus(UUID id, AppointmentStatus status);
    
}
