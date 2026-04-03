package com.githubzs.plataforma_reservas_medicas.domine.repositories;


import java.time.DayOfWeek;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.githubzs.plataforma_reservas_medicas.domine.entities.DoctorSchedule;




public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    Page<DoctorSchedule> findByDoctor_IdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek, Pageable pageable);

}
