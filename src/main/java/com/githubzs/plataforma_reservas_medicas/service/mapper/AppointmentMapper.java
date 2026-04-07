package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;


@Mapper(componentModel = "spring", uses = { 
    PatientSummaryMapper.class, 
    DoctorSummaryMapper.class, 
    OfficeSummaryMapper.class, 
    AppointmentTypeSummaryMapper.class 
})
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Se setea en el servicio
    @Mapping(target = "endAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "observations", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "updatedAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "patient", ignore = true) // Se setea en el servicio
    @Mapping(target = "doctor", ignore = true) // Se setea en el servicio
    @Mapping(target = "office", ignore = true) // Se setea en el servicio
    @Mapping(target = "appointmentType", ignore = true) // Se setea en el servicio
    Appointment toEntity(AppointmentCreateRequest request);

    // — Cancelar cita: actualiza campos sobre la entidad existente —
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // el servicio asigna CANCELLED
    @Mapping(target = "startAt", ignore = true)
    @Mapping(target = "endAt", ignore = true)
    @Mapping(target = "observations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "office", ignore = true)
    @Mapping(target = "appointmentType", ignore = true)
    void applyCancelRequest(AppointmentCancelRequest request, @MappingTarget Appointment appointment);

    // — Completar cita: actualiza campos sobre la entidad existente —
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // el servicio asigna COMPLETED
    @Mapping(target = "startAt", ignore = true)
    @Mapping(target = "endAt", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true) // Se setea en el servicio
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "office", ignore = true)
    @Mapping(target = "appointmentType", ignore = true)
    void applyCompleteRequest(AppointmentCompleteRequest request, @MappingTarget Appointment appointment);

    AppointmentResponse toResponse(Appointment appointment);

}