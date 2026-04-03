package com.githubzs.plataforma_reservas_medicas.domine.entities;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "appointments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    private Doctor doctor;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "office_id", referencedColumnName = "id", nullable = false)
    private Office office;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "appointment_type_id", referencedColumnName = "id", nullable = false)
    private AppointmentType appointmentType;
    
    @NotNull
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Size(max = 1000)
    @Column(name = "cancel_reason", length = 1000)
    private String cancelReason;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observations;

    @NotNull
    @PastOrPresent
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PastOrPresent
    @Column(name = "updated_at")
    private Instant updatedAt;
}
