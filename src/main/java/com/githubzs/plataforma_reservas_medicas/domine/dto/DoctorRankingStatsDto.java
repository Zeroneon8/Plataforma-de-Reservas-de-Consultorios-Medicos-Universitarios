package com.githubzs.plataforma_reservas_medicas.domine.dto;

import java.util.UUID;

import lombok.Getter;

// DTO para representar a un doctor y su cantidad de citas completadas
@Getter
public class DoctorRankingStatsDto {

    private UUID doctorId;

    private String doctorName;

    private long completedAppointments;

    public DoctorRankingStatsDto(UUID doctorId, String doctorName, long completedAppointments) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.completedAppointments = completedAppointments;
    }
    
}
