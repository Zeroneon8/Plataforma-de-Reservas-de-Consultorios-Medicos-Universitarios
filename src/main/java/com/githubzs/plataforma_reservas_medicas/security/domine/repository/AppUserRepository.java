package com.githubzs.plataforma_reservas_medicas.security.domine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.githubzs.plataforma_reservas_medicas.security.domine.entities.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    
    Optional<AppUser> findByDocumentNumberIgnoreCase(String documentNumber);

    boolean existsByDocumentNumberIgnoreCase(String documentNumber);

}
