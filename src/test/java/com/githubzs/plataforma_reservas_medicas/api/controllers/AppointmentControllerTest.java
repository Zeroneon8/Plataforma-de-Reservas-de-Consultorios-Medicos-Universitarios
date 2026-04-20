package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;

import org.junit.jupiter.api.Test;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;

import com.githubzs.plataforma_reservas_medicas.services.AppointmentService;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.*;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.domine.enums.AppointmentStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    AppointmentService appointmentService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();
        
        var request = new AppointmentCreateRequest(baseId, baseId, baseId, baseId, baseDateTime);
        
        var response = new AppointmentResponse(
            baseId,
            null,
            null,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            Instant.now().minusSeconds(6000000),
            null);

        when (appointmentService.create(request)).thenReturn(response);

        mockMvc.perform(
            post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("api/appointments/" + baseId.toString())))
            .andExpect(jsonPath("$.id").value(baseId.toString()));
    }

    @Test
    void getShouldReturn200() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();
        
        var response = new AppointmentResponse(
            baseId,
            null,
            null,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            Instant.now().minusSeconds(6000000),
            null);

        when (appointmentService.findById(baseId)).thenReturn(response);

        mockMvc.perform(get("/api/appointments/{id}", baseId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()));
    }

    @Test
    void getShouldReturn404WhenNotFound() throws Exception {
        var baseId = UUID.randomUUID();

        when(appointmentService.findById(baseId)).thenThrow(new ResourceNotFoundException("Appointment not found"));

        mockMvc.perform(get("/api/appointments/{id}", baseId.toString()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Appointment not found"));
    }

    @Test
    void listShouldReturn200() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();
        var dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        
        var request = new AppointmentSearchRequest(null, null, null, null, baseDateTime, null, null);
        
        var response = new AppointmentSummaryResponse(
            baseId,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.SCHEDULED,
            null,
            null,
            Instant.now().minusSeconds(6000000),
            null);
    
        when (appointmentService.findAll(request, PageRequest.of(0, 10, Sort.by("createdAt").ascending()))).thenReturn(new PageImpl<>(List.of(response)));
    
        mockMvc.perform(
            get("/api/appointments")
                .param("page", "0")
                .param("size", "10")
                .param("startAt", baseDateTime.format(dtFmt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(baseId.toString()));
    }

    @Test
    void listShouldReturn422WhenInvalidDateRange() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        
        var request = new AppointmentSearchRequest(null, null, null, null, baseDateTime, baseDateTime.minusMinutes(30), null);
        
        when (appointmentService.findAll(request, PageRequest.of(0, 10, Sort.by("createdAt").ascending()))).thenThrow(new ValidationException("Start time must be before end time",
            List.of(new FieldViolation("startAt", "must be before or equal to endAt"))));

        mockMvc.perform(
            get("/api/appointments")
                .param("startAt", baseDateTime.format(dtFmt))
                .param("endAt", baseDateTime.minusMinutes(30).format(dtFmt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.message").value("Start time must be before end time"))
            .andExpect(jsonPath("$.violations[0].field").value("startAt"))
            .andExpect(jsonPath("$.violations[0].message").value("must be before or equal to endAt"));
    } 

    @Test
    void patchCancelShouldReturn200() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();
        
        var request = new AppointmentCancelRequest("Patient requested cancellation");

        var response = new AppointmentSummaryResponse(
            baseId,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.CANCELLED,
            "Patient requested cancellation",
            null,
            Instant.now().minusSeconds(6000000),
            null);

        when (appointmentService.cancel(baseId, request)).thenReturn(response);

        mockMvc.perform(
            patch("/api/appointments/{id}/cancel", baseId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()))
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.cancelReason").value("Patient requested cancellation"));
    }

    @Test
    void patchCompleteShouldReturn200() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();
        
        var request = new AppointmentCompleteRequest("Patient has fully recovered");

        var response = new AppointmentSummaryResponse(
            baseId,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.COMPLETED,
            null,
            "Patient has fully recovered",
            Instant.now().minusSeconds(6000000),
            null);

        when (appointmentService.complete(baseId, request)).thenReturn(response);

        mockMvc.perform(
            patch("/api/appointments/{id}/complete", baseId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.observations").value("Patient has fully recovered"));
    }

    @Test
    void patchConfirmShouldReturn200() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();

        var response = new AppointmentSummaryResponse(
            baseId,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.CONFIRMED,
            null,
            null,
            Instant.now().minusSeconds(6000000),
            null);

        when (appointmentService.confirm(baseId)).thenReturn(response);

        mockMvc.perform(
            patch("/api/appointments/{id}/confirm", baseId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void patchNoShowShouldReturn200() throws Exception {
        var baseDateTime = LocalDateTime.of(2026, 3, 4, 10, 0);
        var baseId = UUID.randomUUID();

        var response = new AppointmentSummaryResponse(
            baseId,
            null,
            null,
            baseDateTime,
            baseDateTime.plusMinutes(30),
            AppointmentStatus.NO_SHOW,
            null,
            null,
            Instant.now().minusSeconds(6000000),
            null);

        when (appointmentService.markNoShow(baseId)).thenReturn(response);

        mockMvc.perform(
            patch("/api/appointments/{id}/no-show", baseId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()))
            .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

}
