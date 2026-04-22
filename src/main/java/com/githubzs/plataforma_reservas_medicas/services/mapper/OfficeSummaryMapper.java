package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.Mapper;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;

@Mapper(componentModel = "spring")
public interface OfficeSummaryMapper {

    OfficeSummaryResponse toSummaryResponse(Office office);

}
