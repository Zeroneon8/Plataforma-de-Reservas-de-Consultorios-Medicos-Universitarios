package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.util.UUID;

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
@Table(name = "specialities")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Specialty {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name="name")
    private String name;

    @Column(nullable= false, name= "description")
    private String description;
    
}
