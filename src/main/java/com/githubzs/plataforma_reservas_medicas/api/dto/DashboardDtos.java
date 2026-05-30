package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;

public class DashboardDtos {
    
    public record DashboardResponse(
        long totalDoctors,
        long totalPatients,
        long todayAppointments,
        long todayCompletedAppointments,
        long todayScheduledAppointments
    ) implements Serializable {}  

}