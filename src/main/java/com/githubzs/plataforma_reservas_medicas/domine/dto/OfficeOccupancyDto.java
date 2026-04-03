package com.githubzs.plataforma_reservas_medicas.domine.dto;

import java.util.UUID;

import lombok.Getter;

// DTO para representar la ocupación de un consultorio en un día especifico
@Getter
public class OfficeOccupancyDto {

    private UUID officeId;

    private long appointmentCount;

    private long minutesOccupied;

    private long noShowCount;
    
    public OfficeOccupancyDto(UUID officeId, long appointmentCount, long minutesOccupied, long noShowCount) {
        this.officeId = officeId;
        this.appointmentCount = appointmentCount;
        this.minutesOccupied = minutesOccupied;
        this.noShowCount = noShowCount;
    }

}
    