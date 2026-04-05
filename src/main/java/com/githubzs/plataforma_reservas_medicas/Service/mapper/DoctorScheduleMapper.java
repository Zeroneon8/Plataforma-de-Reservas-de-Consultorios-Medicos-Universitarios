package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDto.DoctorScheduleCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDto.DoctorScheduleResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDto.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {

    @Mapping(target = "id",     ignore = true)
    @Mapping(target = "doctor", source = "doctor")
    DoctorSchedule toEntity(DoctorScheduleCreateRequest request, Doctor doctor);

    @Mapping(target = "doctor.id",               source = "doctor.id")
    @Mapping(target = "doctor.fullName",         source = "doctor.fullName")
    @Mapping(target = "doctor.email",            source = "doctor.email")
    @Mapping(target = "doctor.status",           source = "doctor.status")
    @Mapping(target = "doctor.specialty.id",     source = "doctor.specialty.id")
    @Mapping(target = "doctor.specialty.name",   source = "doctor.specialty.name")
    DoctorScheduleResponse toResponse(DoctorSchedule schedule);

    DoctorScheduleSummaryResponse toSummaryResponse(DoctorSchedule schedule);
}