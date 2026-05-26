package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.enums.PatientStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.services.PatientService;
import com.githubzs.plataforma_reservas_medicas.security.jwt.JwtAuthenticationFilter;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = PatientController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
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

        var response = new PatientResponse(patientId, "John Doe", "john.doe@example.com", "1234567890", PatientStatus.ACTIVE, "123456789", "STU12345", Instant.now(), null, null);

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

        var response = new PatientSummaryResponse(patientId, "John Doe", "john.doe@example.com", "1234567890", PatientStatus.ACTIVE, "1013121361", "2024114001", Instant.now(), null);

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

       var response = new PatientSummaryResponse(patientId, "John Doe", "john.doe@example.com", "1234567890", PatientStatus.ACTIVE, "1013121361", "2024114001", Instant.now(), null);

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

        var request = new PatientUpdateRequest("John Doe Updated", "john.updated@example.com", null, PatientStatus.INACTIVE);

        var response = new PatientResponse(patientId, "John Doe Updated", "john.updated@example.com", "1234567890", PatientStatus.INACTIVE, "123456789", "STU12345", Instant.now(), Instant.now(), null);

        when(patientService.update(patientId, request)).thenReturn(response);

        mockMvc.perform(
            patch("/api/patients/{id}", patientId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientId.toString()))
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateShouldReturn404WhenNotFound() throws Exception {
        var patientId = UUID.randomUUID();

        var request = new PatientUpdateRequest("John Doe Updated", "john.updated@example.com", null, PatientStatus.INACTIVE);

        when(patientService.update(patientId, request)).thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(
            patch("/api/patients/{id}", patientId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

}
