package com.githubzs.plataforma_reservas_medicas.domine.dto;

import java.util.UUID;

import lombok.Getter;

// DTO para representar la ocupación de un consultorio
@Getter
public class OfficeOccupancyDto {

    private UUID officeId;

    private String officeName;

    private String officeLocation;

    private int roomNumber;

    private long appointmentCount;

    private long minutesOccupied;

    private long noShowCount;
    
    public OfficeOccupancyDto(UUID officeId, String officeName, String officeLocation, int roomNumber, long appointmentCount, long minutesOccupied, long noShowCount) {
        this.officeId = officeId;
        this.officeName = officeName;
        this.officeLocation = officeLocation;
        this.roomNumber = roomNumber;
        this.appointmentCount = appointmentCount;
        this.minutesOccupied = minutesOccupied;
        this.noShowCount = noShowCount;
    }

}
    