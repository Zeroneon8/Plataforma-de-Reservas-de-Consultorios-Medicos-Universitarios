package com.githubzs.plataforma_reservas_medicas.domine.dto;

import java.util.UUID;

import lombok.Getter;

// DTO para representar la catidad de inasistencias (NO_SHOW) de un paciente
@Getter
public class PatientNoShowStatsDto {

    private UUID patientId;

    private String patientName;
    
    private long noShowCount;
    
    public PatientNoShowStatsDto(UUID patientId, String patientName, long noShowCount) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.noShowCount = noShowCount;
    }

}
