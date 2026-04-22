package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;

@Mapper(componentModel = "spring", uses = { AppointmentSummaryMapper.class })
public interface OfficeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Se setea en el servicio
    @Mapping(target = "createdAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "updatedAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "appointments", ignore = true)
    Office toEntity(OfficeCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    void patch(OfficeUpdateRequest request, @MappingTarget Office office);

    OfficeResponse toResponse(Office office);
    
}