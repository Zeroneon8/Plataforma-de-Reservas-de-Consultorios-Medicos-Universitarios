package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true) // Se setea en el servicio
    DoctorSchedule toEntity(DoctorScheduleCreateRequest request);

    @Mapping(target = "doctor.id", source = "doctor.id")
    @Mapping(target = "doctor.fullName", source = "doctor.fullName")
    @Mapping(target = "doctor.email", source = "doctor.email")
    @Mapping(target = "doctor.status", source = "doctor.status")
    @Mapping(target = "doctor.createdAt", source = "doctor.createdAt")
    @Mapping(target = "doctor.updatedAt", source = "doctor.updatedAt")
    @Mapping(target = "doctor.specialty.id", source = "doctor.specialty.id")
    @Mapping(target = "doctor.specialty.name", source = "doctor.specialty.name")
    @Mapping(target = "doctor.specialty.description", source = "doctor.specialty.description")
    DoctorScheduleResponse toResponse(DoctorSchedule schedule);

}