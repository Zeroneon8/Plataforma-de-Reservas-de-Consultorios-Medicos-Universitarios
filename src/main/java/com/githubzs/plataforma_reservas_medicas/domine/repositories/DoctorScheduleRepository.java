package com.githubzs.plataforma_reservas_medicas.domine.repositories;


import java.time.DayOfWeek;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    List<DoctorSchedule> findByDoctor_IdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);

    List<DoctorSchedule> findByDoctor_Id(UUID doctorId);

}
