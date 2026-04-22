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

import com.githubzs.plataforma_reservas_medicas.services.SpecialtyService;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.*;

@WebMvcTest(SpecialtyController.class)
public class SpecialtyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SpecialtyService specialtyService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var specialtyId = UUID.randomUUID();

        var request = new SpecialtyCreateRequest("Cardiology", "Heart related specialties");

        var response = new SpecialtyResponse(specialtyId, "Cardiology", "Heart related specialties", null);

        when(specialtyService.create(request)).thenReturn(response);

        mockMvc.perform(
            post("/api/specialties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/specialties/" + specialtyId.toString())))
            .andExpect(jsonPath("$.id").value(specialtyId.toString()));

    }

    @Test
    void listShouldReturn200() throws Exception {
        var specialtyId = UUID.randomUUID();

        var result = List.of(new SpecialtySummaryResponse(specialtyId, "Cardiology", "Heart related specialties"));

        when(specialtyService.findAll()).thenReturn(result);

        mockMvc.perform(get("/api/specialties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(specialtyId.toString()));
    }

}
