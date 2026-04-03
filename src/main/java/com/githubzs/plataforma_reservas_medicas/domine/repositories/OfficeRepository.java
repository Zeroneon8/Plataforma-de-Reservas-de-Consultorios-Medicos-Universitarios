package com.githubzs.plataforma_reservas_medicas.domine.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

public interface OfficeRepository extends JpaRepository<Office, UUID> {

    boolean existsByIdAndStatus(UUID id, OfficeStatus status);

    Page<Office> findByStatus(OfficeStatus status, Pageable pageable);

}
