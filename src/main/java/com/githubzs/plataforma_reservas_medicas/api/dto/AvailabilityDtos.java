package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;


public class AvailabilityDtos {

    public record AvailabilitySlotResponse(
        LocalDate date,
        LocalTime slotStart,
        LocalTime slotEnd
    ) implements Serializable {}


}