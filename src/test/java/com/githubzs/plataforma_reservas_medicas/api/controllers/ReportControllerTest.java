package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

import com.githubzs.plataforma_reservas_medicas.services.ReportService;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.*;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;

@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReportService reportService;

    @Test
    void getDoctorProductivityShouldReturn200() throws Exception {
        var doctorId = UUID.randomUUID();

        var result = List.of(
            new DoctorProductivityResponse(1, doctorId, "Dr. Smith", 20)
        );

        when(reportService.getDoctorProductivity()).thenReturn(result);

        mockMvc.perform(
            get("/api/reports/doctor-productivity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()))
            .andExpect(jsonPath("$[0].doctorFullName").value("Dr. Smith"))
            .andExpect(jsonPath("$[0].completedAppointments").value(20));
    }

    @Test
    void getNoShowPatientsShouldReturn200() throws Exception {
        var from = LocalDate.of(2026, 1, 1);
        var to = LocalDate.of(2026, 1, 31);

        var result = List.of(
            new NoShowPatientResponse(UUID.randomUUID(), "John Doe", 3)
        );

        when(reportService.getNoShowPatients(from, to)).thenReturn(result);

        mockMvc.perform(
            get("/api/reports/no-show-patients")
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].patientFullName").value("John Doe"))
            .andExpect(jsonPath("$[0].totalNoShows").value(3));
    }

    @Test
    void getNoShowPatientsShouldReturn422WhenInvalidDateRange() throws Exception {
        var from = LocalDate.of(2026, 1, 31);
        var to = LocalDate.of(2026, 1, 1);

        when(reportService.getNoShowPatients(from, to)).thenThrow(new ValidationException("From date must be before or equal to to date",
                List.of(new FieldViolation("from", "must be before or equal to to date"))));

        mockMvc.perform(
            get("/api/reports/no-show-patients")
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getOfficeOccupancyShouldReturn200() throws Exception {
        var from = LocalDate.of(2026, 1, 1);
        var to = LocalDate.of(2026, 1, 31);
        var officeId = UUID.randomUUID();

        var result = List.of(
            new OfficeOccupancyResponse(officeId, "Office 1", "Mar caribe", 101, 20, 600, 3)
        );

        when(reportService.getOfficeOccupancy(from, to)).thenReturn(result);

        mockMvc.perform(
            get("/api/reports/office-occupancy")
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].officeId").value(officeId.toString()))
            .andExpect(jsonPath("$[0].officeName").value("Office 1"))
            .andExpect(jsonPath("$[0].officeLocation").value("Mar caribe"))
            .andExpect(jsonPath("$[0].roomNumber").value(101))
            .andExpect(jsonPath("$[0].appointmentCount").value(20))
            .andExpect(jsonPath("$[0].minutesOccupied").value(600))
            .andExpect(jsonPath("$[0].noShowCount").value(3));
    }

    @Test
    void getOfficeOccupancyShouldReturn422WhenInvalidDateRange() throws Exception {
        var from = LocalDate.of(2026, 1, 31);
        var to = LocalDate.of(2026, 1, 1);

        when(reportService.getOfficeOccupancy(from, to)).thenThrow(new ValidationException("From date must be before or equal to to date",
                List.of(new FieldViolation("from", "must be before or equal to to date"))));

        mockMvc.perform(
            get("/api/reports/office-occupancy")
                .param("from", from.toString())
                .param("to", to.toString()))
            .andExpect(status().isUnprocessableContent());
    }

}
