package com.githubzs.plataforma_reservas_medicas.domine.dto;

import java.util.UUID;

import lombok.Getter;

// DTO para representar las estadísticas de asistencia (cancelaciones y no-shows) por especialidad
@Getter
public class SpecialtyAttendanceStatsDto {

    private UUID specialtyId;
    
    private String specialtyName;
    
    private long cancelledCount;
    
    private long noShowCount;

    public SpecialtyAttendanceStatsDto(UUID specialtyId, String specialtyName, long cancelledCount, long noShowCount) {
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.cancelledCount = cancelledCount;
        this.noShowCount = noShowCount;
    }

}
