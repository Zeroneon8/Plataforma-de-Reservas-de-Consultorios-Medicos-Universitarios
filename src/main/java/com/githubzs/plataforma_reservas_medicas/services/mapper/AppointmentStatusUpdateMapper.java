package com.githubzs.plataforma_reservas_medicas.services.mapper;

import org.mapstruct.Mapper;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentStatusUpdateResponse;
import com.githubzs.plataforma_reservas_medicas.domine.entities.Appointment;

@Mapper(componentModel = "spring", uses = { 
    PatientSummaryMapper.class, 
    DoctorSummaryMapper.class
})
public interface AppointmentStatusUpdateMapper {
    
    AppointmentStatusUpdateResponse toStatusUpdateResponse(Appointment appointment);

}
