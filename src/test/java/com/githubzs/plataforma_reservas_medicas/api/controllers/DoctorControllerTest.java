package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

import com.githubzs.plataforma_reservas_medicas.services.DoctorService;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.enums.DoctorStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;

@WebMvcTest(DoctorController.class)
public class DoctorControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DoctorService doctorService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var baseId = UUID.randomUUID();

        var request = new DoctorCreateRequest("Dr. House", "house@example.com", "12345", "67890", baseId);

        var response = new DoctorResponse(baseId, "Dr. House", "house@example.com", null, DoctorStatus.ACTIVE, Instant.now(), null, null, null);

        when(doctorService.create(request)).thenReturn(response);

        mockMvc.perform(
            post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/doctors/" + baseId.toString())))
            .andExpect(jsonPath("$.id").value(baseId.toString()));
    }

    @Test
    void getShouldReturn200() throws Exception {
        var baseId = UUID.randomUUID();

        var response = new DoctorSummaryResponse(baseId, "Dr. House", "house@example.com", null, DoctorStatus.ACTIVE, Instant.now(), null);

        when(doctorService.findById(baseId)).thenReturn(response);

        mockMvc.perform(
            get("/api/doctors/" + baseId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()));
    }

    @Test
    void getShouldReturn404WhenNotFound() throws Exception {
        var baseId = UUID.randomUUID();

        when(doctorService.findById(baseId)).thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(
            get("/api/doctors/" + baseId.toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    void listShouldReturn200() throws Exception {
        var baseId = UUID.randomUUID();

        var result = List.of(
            new DoctorSummaryResponse(baseId, "Dr. House", "house@example.com", null, DoctorStatus.ACTIVE, Instant.now(), null));

        when(doctorService.findAll(PageRequest.of(0, 10, Sort.by("createdAt").ascending()))).thenReturn(new PageImpl<>(result));

        mockMvc.perform(
            get("/api/doctors")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(baseId.toString()));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        var baseId = UUID.randomUUID();

        var request = new DoctorUpdateRequest("Dr. House", "house@example.com", null);

        var response = new DoctorSummaryResponse(baseId, "Dr. House", "house@example.com", null, DoctorStatus.ACTIVE, Instant.now(), null);

        when(doctorService.update(baseId, request)).thenReturn(response);

        mockMvc.perform(
            patch("/api/doctors/" + baseId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(baseId.toString()));
    }

    @Test
    void updateShouldReturn404WhenDoctorNotFound() throws Exception {
        var baseId = UUID.randomUUID();

        var request = new DoctorUpdateRequest("Dr. House", "house@example.com", null);

        when(doctorService.update(baseId, request)).thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(
            patch("/api/doctors/" + baseId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateShouldReturn404WhenSpecialtyNotFound() throws Exception {
        var baseId = UUID.randomUUID();

        var request = new DoctorUpdateRequest("Dr. House", "house@example.com", baseId);

        when(doctorService.update(baseId, request)).thenThrow(new ResourceNotFoundException("Specialty not found"));

        mockMvc.perform(
            patch("/api/doctors/" + baseId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

}