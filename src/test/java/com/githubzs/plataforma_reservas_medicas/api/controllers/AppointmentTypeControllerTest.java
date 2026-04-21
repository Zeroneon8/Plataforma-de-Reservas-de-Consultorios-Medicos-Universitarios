package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.util.List;

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

import com.githubzs.plataforma_reservas_medicas.services.AppointmentTypeService;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.*;

@WebMvcTest(AppointmentTypeController.class)
public class AppointmentTypeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AppointmentTypeService appointmentTypeService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        UUID baseId = UUID.randomUUID();

        var request = new AppointmentTypeCreateRequest("Consulta", "Consulta médica geral", 30);

        var response = new AppointmentTypeResponse(baseId, "Consulta", "Consulta médica geral", 30, null);

        when (appointmentTypeService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/appointment-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/appointment-types")));
    }

    @Test
    void listShouldReturn200() throws Exception {
        var baseId = UUID.randomUUID();

        var result = List.of(new AppointmentTypeSummaryResponse(baseId, "Consulta", "Consulta médica geral", 30)); 

        when (appointmentTypeService.findAll()).thenReturn(result);

        mockMvc.perform(get("/api/appointment-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(baseId.toString()))
            .andExpect(jsonPath("$[0].name").value("Consulta"))
            .andExpect(jsonPath("$[0].description").value("Consulta médica geral"))
            .andExpect(jsonPath("$[0].durationMinutes").value(30));
    }

}
