package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;

@Mapper(componentModel = "spring")
public interface DoctorScheduleSummaryMapper {

    DoctorScheduleSummaryResponse toSummaryResponse(DoctorSchedule schedule);

}