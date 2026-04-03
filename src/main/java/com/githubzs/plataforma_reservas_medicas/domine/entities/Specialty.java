package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "specialities")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Specialty {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "specialty")
    @Builder.Default
    private Set<Doctor> doctors = new HashSet<>();

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
        doctor.setSpecialty(this);
    }
}
