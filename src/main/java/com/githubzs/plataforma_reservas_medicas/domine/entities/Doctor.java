package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "doctors")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @NotBlank
    @Size(max = 50)
    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @NotBlank
    @Size(max = 320)
    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorStatus status;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "specialty_id", referencedColumnName = "id", nullable = false)
    private Specialty specialty;

    @NotNull
    @PastOrPresent
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PastOrPresent
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private Set<Appointment> appointments = new HashSet<>();

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private Set<DoctorSchedule> schedules = new HashSet<>();
}
