package com.githubzs.plataforma_reservas_medicas.Service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorDto.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorDto.DoctorDetailResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorDto.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorDto.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

@Mapper(componentModel = "spring", uses = { SpecialtyMapper.class, DoctorScheduleMapper.class, AppointmentMapper.class })
public interface DoctorMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true) // el servicio asigna ACTIVE
    @Mapping(target = "createdAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "updatedAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "schedules",    ignore = true)
    @Mapping(target = "specialty",    source = "specialty")
    Doctor toEntity(DoctorCreateRequest request, Specialty specialty);

    @Mapping(target = "specialty", source = "specialty")
    DoctorResponse toResponse(Doctor doctor);

    @Mapping(target = "specialty", source = "specialty")
    DoctorSummaryResponse toSummaryResponse(Doctor doctor);

    @Mapping(target = "specialty",     source = "specialty")
    @Mapping(target = "schedules",     source = "schedules")
    @Mapping(target = "appointments",  source = "appointments")
    DoctorDetailResponse toDetailResponse(Doctor doctor);
}