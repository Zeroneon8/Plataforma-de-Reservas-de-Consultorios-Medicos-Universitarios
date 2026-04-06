package com.githubzs.plataforma_reservas_medicas.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleResponse;

public interface DoctorScheduleService {

    DoctorScheduleResponse create(UUID doctorId, DoctorScheduleCreateRequest request);

    List<DoctorScheduleResponse> findByDoctorAndDay(UUID doctorId, DayOfWeek day); 

    List<DoctorScheduleResponse> findByDoctor(UUID doctorId); 
    
    boolean isWithinSchedule(UUID doctorId, LocalDateTime start, LocalDateTime end); // Regla negocio: Verifica que el rango start-end cae dentro de un bloque horario del doctor para ese día de semana. Usado por AppointmentService.
  
}