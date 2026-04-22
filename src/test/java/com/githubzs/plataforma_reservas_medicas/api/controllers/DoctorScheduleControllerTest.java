package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalTime;

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

import com.githubzs.plataforma_reservas_medicas.services.DoctorScheduleService;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.*;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.exception.ResourceNotFoundException;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;

@WebMvcTest(DoctorScheduleController.class)
public class DoctorScheduleControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DoctorScheduleService doctorScheduleService;

    @Test
    void createShouldReturn201AndLocation() throws Exception {
        var baseId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();
        var startTime = LocalTime.of(9, 0);
        var endTime = LocalTime.of(17, 0);

        var request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, startTime, endTime);

        var response = new DoctorScheduleResponse(baseId, null, DayOfWeek.MONDAY, startTime, endTime);

        when(doctorScheduleService.create(doctorId, request)).thenReturn(response);

        mockMvc.perform(
            post("/api/doctors/{id}/schedules", doctorId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/doctors/" + doctorId.toString() + "/schedules")))
            .andExpect(jsonPath("$.id").value(baseId.toString()));
    }

    @Test
    void createShouldReturn404WhenDoctorNotFound() throws Exception {
        var doctorId = UUID.randomUUID();
        var startTime = LocalTime.of(9, 0);
        var endTime = LocalTime.of(17, 0);

        var request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, startTime, endTime);

        when(doctorScheduleService.create(doctorId, request)).thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(
            post("/api/doctors/{id}/schedules", doctorId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void createShouldReturn422WhenInvalidTimeRange() throws Exception {
        var doctorId = UUID.randomUUID();
        var startTime = LocalTime.of(17, 0);
        var endTime = LocalTime.of(9, 0);

        var request = new DoctorScheduleCreateRequest(DayOfWeek.MONDAY, startTime, endTime);

        when(doctorScheduleService.create(doctorId, request)).thenThrow(new ValidationException("Start time and end time are required",
                List.of(new FieldViolation("startTime", "is required"), new FieldViolation("endTime", "is required"))));

        mockMvc.perform(
            post("/api/doctors/{id}/schedules", doctorId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getByDoctorShouldReturn200() throws Exception {
        var doctorId = UUID.randomUUID();
        var scheduleId = UUID.randomUUID();

        var result = List.of(
            new DoctorScheduleSummaryResponse(scheduleId, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));

        when(doctorScheduleService.findByDoctor(doctorId)).thenReturn(result);

        mockMvc.perform(
            get("/api/doctors/{id}/schedules", doctorId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(scheduleId.toString()));
    }

}
