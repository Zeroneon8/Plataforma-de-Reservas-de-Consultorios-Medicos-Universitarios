package com.githubzs.plataforma_reservas_medicas.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentCompleteRequestDto;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDto.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;
import com.githubzs.plataforma_reservas_medicas.domine.entities.AppointmentType;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Doctor;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Office;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Patient;


@Mapper(componentModel = "spring", uses = { 
    PatientMapper.class, 
    DoctorMapper.class, 
    OfficeMapper.class, 
    AppointmentTypeMapper.class 
})
public interface AppointmentMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "endAt",     ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "observations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "patient",         source = "patient")
    @Mapping(target = "doctor",          source = "doctor")
    @Mapping(target = "office",          source = "office")
    @Mapping(target = "appointmentType", source = "appointmentType")
    Appointment toEntity(AppointmentCreateRequest request, Patient patient,Doctor doctor, Office office,AppointmentType appointmentType);

    // — Cancelar cita: actualiza campos sobre la entidad existente —
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "status",          ignore = true) // el servicio asigna CANCELLED
    @Mapping(target = "startAt",         ignore = true)
    @Mapping(target = "endAt",           ignore = true)
    @Mapping(target = "observations",    ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true) // el servicio lo asigna
    @Mapping(target = "patient",         ignore = true)
    @Mapping(target = "doctor",          ignore = true)
    @Mapping(target = "office",          ignore = true)
    @Mapping(target = "appointmentType", ignore = true)
    void applyCancelRequest(AppointmentCancelRequest request, @MappingTarget Appointment appointment);

    // — Completar cita: actualiza campos sobre la entidad existente —
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "status",          ignore = true) // el servicio asigna COMPLETED
    @Mapping(target = "startAt",         ignore = true)
    @Mapping(target = "endAt",           ignore = true)
    @Mapping(target = "cancelReason",    ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true) // el servicio lo asigna
    @Mapping(target = "patient",         ignore = true)
    @Mapping(target = "doctor",          ignore = true)
    @Mapping(target = "office",          ignore = true)
    @Mapping(target = "appointmentType", ignore = true)
    void applyCompleteRequest(AppointmentCompleteRequestDto request, @MappingTarget Appointment appointment);

    AppointmentResponse toResponse(Appointment appointment);

    AppointmentSummaryResponse toSummaryResponse(Appointment appointment);
}