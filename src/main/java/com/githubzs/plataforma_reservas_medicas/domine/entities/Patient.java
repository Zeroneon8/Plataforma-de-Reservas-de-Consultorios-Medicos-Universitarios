package com.githubzs.plataforma_reservas_medicas.domine.entities;


import java.time.Instant;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name="full_name")
    private String fullName;

    @Column(nullable= false, name= "email", unique= true)
    private String email;

    @Column(nullable = false, name = "phone_number", unique=true)
    private String phoneNumber;

    @Column(nullable = false, name = "document_number", unique=true) 
    private String documentNumber;

    @Column(nullable = true, name = "student_code", unique=true)
    private String studentCode;

    @Column(nullable = false, name = "status")
    private PatientStatus status;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

}
