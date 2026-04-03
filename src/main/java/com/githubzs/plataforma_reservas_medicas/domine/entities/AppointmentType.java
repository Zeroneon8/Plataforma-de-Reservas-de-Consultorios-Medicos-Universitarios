package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment_types")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentType {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false,unique = true, length = 100)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Positive
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
}
