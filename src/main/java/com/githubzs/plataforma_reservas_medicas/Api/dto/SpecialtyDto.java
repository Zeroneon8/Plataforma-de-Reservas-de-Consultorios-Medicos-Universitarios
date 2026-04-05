package com.githubzs.plataforma_reservas_medicas.Api.dto;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.Api.dto.DoctorDto.DoctorSummaryResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SpecialtyDto {
     public record SpecialtyCreateRequest( 
        @NotBlank @Size(max = 100) 
        String name, 
        @Size(max = 255) 
        String description
    ) implements Serializable{}

     public record SpecialtyResponse(
        UUID id, 
        String name, 
        String description, 
        Set<DoctorSummaryResponse> doctors
    ) implements Serializable{}

    public record SpecialtySummaryResponse(
        UUID id, 
        String name, 
        String description
    ) implements Serializable{}

}
