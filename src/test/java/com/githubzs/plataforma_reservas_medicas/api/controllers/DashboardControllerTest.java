package com.githubzs.plataforma_reservas_medicas.api.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.githubzs.plataforma_reservas_medicas.api.dto.DashboardDtos.DashboardResponse;
import com.githubzs.plataforma_reservas_medicas.api.error.ApiError.FieldViolation;
import com.githubzs.plataforma_reservas_medicas.exception.ValidationException;
import com.githubzs.plataforma_reservas_medicas.security.jwt.JwtAuthenticationFilter;
import com.githubzs.plataforma_reservas_medicas.services.DashboardService;

@WebMvcTest(
    controllers = DashboardController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DashboardService dashboardService;

    @Test
    void shouldReturn200WhenDashboardStatsAreRequested() throws Exception {
        var date = LocalDate.of(2026, 5, 29);

        var response = new DashboardResponse(
            3L,
            1L,
            10L,
            2L,
            12L,
            4L,
            8L
        );

        when(dashboardService.getDashboardStats(date)).thenReturn(response);

        mockMvc.perform(
            get("/api/dashboard")
                .param("date", date.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeDoctors").value(3))
            .andExpect(jsonPath("$.inactiveDoctors").value(1))
            .andExpect(jsonPath("$.activePatients").value(10))
            .andExpect(jsonPath("$.inactivePatients").value(2))
            .andExpect(jsonPath("$.todayAppointments").value(12))
            .andExpect(jsonPath("$.todayCompletedAppointments").value(4))
            .andExpect(jsonPath("$.todayScheduledAppointments").value(8));
    }

    @Test
    void shouldReturn422WhenDashboardServiceRejectsFutureDate() throws Exception {
        var futureDate = LocalDate.now().plusDays(1);

        when(dashboardService.getDashboardStats(futureDate))
            .thenThrow(new ValidationException("Date cannot be in the future",
                List.of(new FieldViolation("date", "cannot be in the future"))));

        mockMvc.perform(
            get("/api/dashboard")
                .param("date", futureDate.toString()))
            .andExpect(status().isUnprocessableContent());
    }

}
