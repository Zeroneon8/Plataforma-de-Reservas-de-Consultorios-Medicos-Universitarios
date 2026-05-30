package com.githubzs.plataforma_reservas_medicas.services;

import java.time.LocalDate;
import com.githubzs.plataforma_reservas_medicas.api.dto.DashboardDtos.*;

public interface DashboardService {

    DashboardResponse getDashboardStats(LocalDate date);
    
}
