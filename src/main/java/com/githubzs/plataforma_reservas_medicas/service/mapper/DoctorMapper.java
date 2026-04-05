package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorDetailResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "schedules",    ignore = true)
    @Mapping(target = "specialty",    ignore = true) // el servicio resuelve Specialty por specialtyId
    void applyUpdate(DoctorUpdateRequest request, @MappingTarget Doctor doctor);

    @Mapping(target = "specialty", source = "specialty")
    DoctorResponse toResponse(Doctor doctor);

    @Mapping(target = "specialty", source = "specialty")
    DoctorSummaryResponse toSummaryResponse(Doctor doctor);

    @Mapping(target = "specialty",     source = "specialty")
    @Mapping(target = "schedules",     source = "schedules")
    @Mapping(target = "appointments",  source = "appointments")
    DoctorDetailResponse toDetailResponse(Doctor doctor);
}