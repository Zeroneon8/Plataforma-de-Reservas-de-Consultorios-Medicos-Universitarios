package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;

@Mapper(componentModel = "spring", uses = { SpecialtySummaryMapper.class, DoctorScheduleSummaryMapper.class, AppointmentSummaryMapper.class })
public interface DoctorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Se setea en el servicio
    @Mapping(target = "createdAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "updatedAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "specialty", ignore = true) // Se setea en el servicio
    Doctor toEntity(DoctorCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licenseNumber", ignore = true)
    @Mapping(target = "documentNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    void patch(DoctorUpdateRequest changes, @MappingTarget Doctor target);

    @Mapping(target = "specialty",     source = "specialty")
    @Mapping(target = "schedules",     source = "schedules")
    @Mapping(target = "appointments",  source = "appointments")
    DoctorResponse toResponse(Doctor doctor);
    
}