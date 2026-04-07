package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.SpecialtyDtos.SpecialtyResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Specialty;

@Mapper(componentModel = "spring", uses = { DoctorSummaryMapper.class })
public interface SpecialtyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    Specialty toEntity(SpecialtyCreateRequest request);
   
    SpecialtyResponse toResponse(Specialty specialty);

}