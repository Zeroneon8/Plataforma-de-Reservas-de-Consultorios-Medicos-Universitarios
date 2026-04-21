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

import com.githubzs.plataforma_reservas_medicas.services.DoctorService;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.*;

@WebMvcTest(DoctorController.class)
public class DoctorControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DoctorService doctorService;

    @Test
    void createShouldReturn201AndLocation() {
        
    }

    @Test
    void getShouldReturn200() {

    }

    @Test
    void listShouldReturn200() {

    }

    @Test
    void updateShouldReturn200() {

    }

}
