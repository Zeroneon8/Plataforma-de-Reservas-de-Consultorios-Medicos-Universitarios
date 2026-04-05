package com.githubzs.plataforma_reservas_medicas.Service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.githubzs.plataforma_reservas_medicas.Api.dto.AppointmentDto.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.Api.dto.AppointmentDto.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.Api.dto.AppointmentDto.AppointmentSummaryResponse;
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

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "endAt",           ignore = true)
    @Mapping(target = "cancelReason",    ignore = true)
    @Mapping(target = "observations",    ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "patient",         source = "patient")
    @Mapping(target = "doctor",          source = "doctor")
    @Mapping(target = "office",          source = "office")
    @Mapping(target = "appointmentType", source = "appointmentType")
    @Mapping(target = "startAt",         source = "request.startAt")
    Appointment toEntity(
        AppointmentCreateRequest request,
        Patient patient,
        Doctor doctor,
        Office office,
        AppointmentType appointmentType
    );

    AppointmentResponse toResponse(Appointment appointment);

    AppointmentSummaryResponse toSummaryResponse(Appointment appointment);
}