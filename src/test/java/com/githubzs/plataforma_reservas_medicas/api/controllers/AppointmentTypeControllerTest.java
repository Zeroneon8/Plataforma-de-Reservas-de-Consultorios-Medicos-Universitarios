package com.githubzs.plataforma_reservas_medicas.api.controllers;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import com.githubzs.plataforma_reservas_medicas.services.AppointmentTypeService;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.*;

public class AppointmentTypeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    AppointmentTypeService appointmentTypeService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var request = new AppointmentTypeCreateRequest("Consulta", "Consulta médica geral", 30);
    }

    @Test
    void listShouldReturn200() {

    }
}
