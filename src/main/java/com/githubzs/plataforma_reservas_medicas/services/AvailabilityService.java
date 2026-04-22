package com.githubzs.plataforma_reservas_medicas.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.AvailabilitySlotResponse;

public interface AvailabilityService {
    
    List<AvailabilitySlotResponse> getAvailableSlots(
        UUID doctorId,
        LocalDate date
    );

    List<AvailabilitySlotResponse> getAvailableSlotsForAppointmentType(
        UUID doctorId,
        LocalDate date,
        UUID appointmentTypeId
    );

}
