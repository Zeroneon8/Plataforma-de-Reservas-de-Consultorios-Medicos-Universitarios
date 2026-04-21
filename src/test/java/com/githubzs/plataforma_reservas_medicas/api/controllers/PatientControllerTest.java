package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.util.List;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

import com.githubzs.plataforma_reservas_medicas.services.PatientService;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;

@WebMvcTest(PatientController.class)
public class PatientControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PatientService patientService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var patientId = UUID.randomUUID();

        var request = new PatientCreateRequest("John Doe", "john.doe@example.com", "1234567890", "123456789", "STU12345");

        var response = new PatientResponse(patientId, "John Doe", "john.doe@example.com", "1234567890", PatientStatus.ACTIVE, Instant.now(), null, null);

        when(patientService.create(request)).thenReturn(response);

        mockMvc.perform(
            post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/patients/" + patientId.toString())))
            .andExpect(jsonPath("$.id").value(patientId.toString()));
    }

    @Test
    void getShouldReturn200() throws Exception {
        var patientId = UUID.randomUUID();

        var response = new PatientSummaryResponse(patientId, "John Doe", "john.doe@example.com", "1234567890", PatientStatus.ACTIVE, Instant.now(), null);

        when(patientService.findById(patientId)).thenReturn(response);

        mockMvc.perform(
            get("/api/patients/{id}", patientId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientId.toString()));
    }

    @Test
    void getShouldReturn404WhenNotFound() throws Exception {
        var patientId = UUID.randomUUID();

        when(patientService.findById(patientId)).thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(
            get("/api/patients/{id}", patientId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void listShouldReturn200() throws Exception {
        var patientId = UUID.randomUUID();

        var response = new PatientSummaryResponse(patientId, "John Doe", "john.doe@example.com", "1234567890", PatientStatus.ACTIVE, Instant.now(), null);

        when(patientService.findAll(PageRequest.of(0, 10, Sort.by("createdAt").ascending()))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(
            get("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(patientId.toString()));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        var patientId = UUID.randomUUID();

        var request = new PatientUpdateRequest("John Doe Updated", "john.updated@example.com", null);

        var response = new PatientResponse(patientId, "John Doe Updated", "john.updated@example.com", "1234567890", PatientStatus.ACTIVE, Instant.now(), Instant.now(), null);

        when(patientService.update(patientId, request)).thenReturn(response);

        mockMvc.perform(
            patch("/api/patients/{id}", patientId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientId.toString()));
    }

    @Test
    void updateShouldReturn404WhenNotFound() throws Exception {
        var patientId = UUID.randomUUID();

        var request = new PatientUpdateRequest("John Doe Updated", "john.updated@example.com", null);

        when(patientService.update(patientId, request)).thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(
            patch("/api/patients/{id}", patientId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

}
