package com.githubzs.plataforma_reservas_medicas.Service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.githubzs.plataforma_reservas_medicas.Api.dto.OfficeDto.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.Api.dto.OfficeDto.OfficeDetailResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.OfficeDto.OfficeResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.OfficeDto.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.OfficeDto.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;

@Mapper(componentModel = "spring", uses = { AppointmentMapper.class })
public interface OfficeMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true) // el servicio asigna ACTIVE
    @Mapping(target = "createdAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "updatedAt",    ignore = true) // el servicio lo asigna
    @Mapping(target = "appointments", ignore = true)
    Office toEntity(OfficeCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "appointments", ignore = true)
    void applyUpdate(OfficeUpdateRequest request, @MappingTarget Office office);

    OfficeResponse toResponse(Office office);

    OfficeSummaryResponse toSummaryResponse(Office office);

   
    OfficeDetailResponse toDetailResponse(Office office);
}