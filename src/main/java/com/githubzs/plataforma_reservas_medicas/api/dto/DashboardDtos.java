package com.githubzs.plataforma_reservas_medicas.api.dto;

import java.io.Serializable;

public class DashboardDtos {
    
    public record DashboardResponse(
        long activeDoctors,
        long inactiveDoctors,
        long activePatients,
        long inactivePatients,
        long todayAppointments,
        long todayCompletedAppointments,
        long todayScheduledAppointments
    ) implements Serializable {}  

}