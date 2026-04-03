package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.time.Instant;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank
    @Size(max = 320)
    @Column(nullable = false, length = 320)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @NotBlank
    @Size(max = 50)
    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Size(max = 50)
    @Column(name = "student_code", unique = true, length = 50)
    private String studentCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status;

    @NotNull
    @PastOrPresent
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PastOrPresent
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
