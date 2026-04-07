package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;

@Mapper(componentModel = "spring")
public interface AppointmentTypeSummaryMapper {

    AppointmentTypeSummaryResponse toSummaryResponse(AppointmentType appointmentType);

}