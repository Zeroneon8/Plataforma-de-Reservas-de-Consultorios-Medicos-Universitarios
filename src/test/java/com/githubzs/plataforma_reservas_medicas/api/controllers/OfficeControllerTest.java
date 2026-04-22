package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.util.List;
import java.time.Instant;

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

import com.githubzs.plataforma_reservas_medicas.services.OfficeService;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.*;
import com.githubzs.plataforma_reservas_medicas.domine.enums.OfficeStatus;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;

@WebMvcTest(OfficeController.class)
public class OfficeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OfficeService officeService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var officeId = UUID.randomUUID();

        var request = new OfficeCreateRequest("Office 1", "Mar caribe", null, 101);

        var response = new OfficeResponse(officeId, "Office 1", "Mar caribe", null, 101, OfficeStatus.AVAILABLE, Instant.now(), null, null);

        when(officeService.create(request)).thenReturn(response);

        mockMvc.perform(
            post("/api/offices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/offices/" + officeId.toString())))
            .andExpect(jsonPath("$.id").value(officeId.toString()));
    }

    @Test
    void listShouldReturn200() throws Exception {
        var officeId = UUID.randomUUID();

        var result = List.of(new OfficeSummaryResponse(officeId, "Office 1", "Mar caribe", 101, OfficeStatus.AVAILABLE, Instant.now(), null));

        when(officeService.findAll()).thenReturn(result);

        mockMvc.perform(
            get("/api/offices")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(officeId.toString()));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        var officeId = UUID.randomUUID();

        var request = new OfficeUpdateRequest("Office 1 Updated", "Mar caribe updated", null, 102);

        var response = new OfficeResponse(officeId, "Office 1 Updated", "Mar caribe updated", null , 102, OfficeStatus.AVAILABLE, Instant.now(), Instant.now(), null);

        when(officeService.update(officeId, request)).thenReturn(response);

        mockMvc.perform(
            patch("/api/offices/{id}", officeId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(officeId.toString()))
            .andExpect(jsonPath("$.name").value("Office 1 Updated"))
            .andExpect(jsonPath("$.location").value("Mar caribe updated"))
            .andExpect(jsonPath("$.roomNumber").value(102));
    }

    @Test
    void updateShouldReturn404WhenNotFound() throws Exception {
        var officeId = UUID.randomUUID();

        var request = new OfficeUpdateRequest("Office 1 Updated", "Mar caribe updated", null, 102);

        when(officeService.update(officeId, request)).thenThrow(new ResourceNotFoundException("Office not found"));

        mockMvc.perform(
            patch("/api/offices/{id}", officeId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
}
