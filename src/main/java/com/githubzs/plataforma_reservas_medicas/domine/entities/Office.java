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
@Table(name = "offices")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Office {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name="name")
    private String name;

    @Column(nullable= false, name= "location")
    private String location;


    @Column(nullable = true, name = "description")
    private String description;

    @Column(nullable = false, name = "room_number")
    private int roomNumber;

   /*  @Column(nullable = false, name = "room_number")
    private String roomNumber;*/

    @Column(nullable = false, name = "status")
    private PatientStatus status;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;




    
}
