package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;

@Mapper(componentModel = "spring", uses = { SpecialtySummaryMapper.class })
public interface DoctorSummaryMapper {

    @Mapping(target = "specialty", source = "specialty")
    DoctorSummaryResponse toSummaryResponse(Doctor doctor);

}
