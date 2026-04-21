package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.util.List;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

import com.githubzs.plataforma_reservas_medicas.services.AvailabilityService;
import com.githubzs.plataforma_reservas_medicas.api.dto.AvailabilityDtos.*;

@WebMvcTest(AvailabilityController.class)
public class AvailabilityControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    AvailabilityService availabilityService;

    @Test
    void getAvailableSlotsShouldReturn200() throws Exception {
        var doctorId = UUID.randomUUID();
        var baseDate = java.time.LocalDate.of(2026, 1, 1);
        var slotStart = java.time.LocalTime.of(9, 0);
        var slotEnd = java.time.LocalTime.of(9, 30);

        var result = List.of(new AvailabilitySlotResponse(baseDate, slotStart, slotEnd));

        when (availabilityService.getAvailableSlots(doctorId, baseDate)).thenReturn(result);

        mockMvc.perform(
            get("/api/availability/doctors/{doctorId}", doctorId.toString())
                .param("date", baseDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].date").value(baseDate.toString()))
            .andExpect(jsonPath("$[0].slotStart").value(slotStart.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
            .andExpect(jsonPath("$[0].slotEnd").value(slotEnd.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
    }

    @Test
    void getAvailableSlotsForAppointmentTypeShouldReturn200() throws Exception {
        var doctorId = UUID.randomUUID();
        var appointmentTypeId = UUID.randomUUID();
        var baseDate = java.time.LocalDate.of(2026, 1, 1);
        var slotStart = java.time.LocalTime.of(9, 0);
        var slotEnd = java.time.LocalTime.of(9, 30);

        var result = List.of(new AvailabilitySlotResponse(baseDate, slotStart, slotEnd));

        when (availabilityService.getAvailableSlotsForAppointmentType(doctorId, baseDate, appointmentTypeId)).thenReturn(result);

        mockMvc.perform(
            get("/api/availability/doctors/{doctorId}/appointment-types/{appointmentTypeId}", doctorId.toString(), appointmentTypeId.toString())
                .param("date", baseDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].date").value(baseDate.toString()))
            .andExpect(jsonPath("$[0].slotStart").value(slotStart.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
            .andExpect(jsonPath("$[0].slotEnd").value(slotEnd.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
    }

}
