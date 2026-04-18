package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.AvailabilitySlotResponse;
import com.githubzs.plataforma_reservas_medicas.services.AvailabilityService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Validated
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlots(
        @PathVariable UUID doctorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
          var slots = availabilityService.getAvailableSlots(doctorId, date);

        return ResponseEntity.ok(slots);
    }

    @GetMapping("/doctors/{doctorId}/appointment-types/{appointmentTypeId}")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlotsForAppointmentType(
        @PathVariable UUID doctorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) UUID appointmentTypeId
    ) {
        
           var slots = availabilityService.getAvailableSlotsForAppointmentType(doctorId, date, appointmentTypeId);
            
        return ResponseEntity.ok(slots);
    }

}
