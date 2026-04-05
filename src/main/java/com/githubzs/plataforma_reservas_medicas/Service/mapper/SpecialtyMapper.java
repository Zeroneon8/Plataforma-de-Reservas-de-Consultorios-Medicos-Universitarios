package com.githubzs.plataforma_reservas_medicas.Service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.Api.dto.SpecialtyDto.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.Api.dto.SpecialtyDto.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.SpecialtyDto.SpecialtySummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

@Mapper(componentModel = "spring", uses = { DoctorMapper.class })
public interface SpecialtyMapper {

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "doctors", ignore = true)
    Specialty toEntity(SpecialtyCreateRequest request);

   
    SpecialtyResponse toResponse(Specialty specialty);

    SpecialtySummaryResponse toSummaryResponse(Specialty specialty);
}