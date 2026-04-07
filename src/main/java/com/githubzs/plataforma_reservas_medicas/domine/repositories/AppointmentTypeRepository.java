package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, UUID> {

    boolean existsByNameIgnoreCase(String name);

}
