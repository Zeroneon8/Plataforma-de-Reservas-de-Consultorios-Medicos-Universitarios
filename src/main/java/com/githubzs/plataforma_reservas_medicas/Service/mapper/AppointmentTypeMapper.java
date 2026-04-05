package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDto.AppointmentTypeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDto.AppointmentTypeDetailResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDto.AppointmentTypeResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;

@Mapper(componentModel = "spring")
public interface AppointmentTypeMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "appointments", ignore = true)
    AppointmentType toEntity(AppointmentTypeCreateRequest request);

    AppointmentTypeResponse toResponse(AppointmentType appointmentType);

    AppointmentSummaryResponse toSummaryResponse(Appointment appointment);
    
    AppointmentTypeDetailResponse toDetailResponse(AppointmentType appointmentType);
}