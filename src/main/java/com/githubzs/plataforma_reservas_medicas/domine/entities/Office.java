package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String location;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Positive
    @Column(name = "room_number", nullable = false)
    private int roomNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfficeStatus status;

    @NotNull
    @PastOrPresent
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PastOrPresent
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "office")
    @Builder.Default
    private Set<Appointment> appointments = new HashSet<>();

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setOffice(this);
    }
}
