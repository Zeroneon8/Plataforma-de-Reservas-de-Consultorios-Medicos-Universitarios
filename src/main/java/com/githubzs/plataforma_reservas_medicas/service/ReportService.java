package com.githubzs.plataforma_reservas_medicas.service;

import java.time.LocalDate;
import java.util.List;

import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.DoctorProductivityResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.NoShowPatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.ReportDtos.OfficeOccupancyResponse;

public interface ReportService {

    List<OfficeOccupancyResponse> getOfficeOccupancy(LocalDate from, LocalDate to);

    List<DoctorProductivityResponse> getDoctorProductivity();

    List<NoShowPatientResponse> getNoShowPatients(LocalDate from, LocalDate to);
    
}
