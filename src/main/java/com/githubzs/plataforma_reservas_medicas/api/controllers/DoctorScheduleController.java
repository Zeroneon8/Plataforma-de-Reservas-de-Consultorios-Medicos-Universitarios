package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorScheduleDtos.DoctorScheduleSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.services.DoctorScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping("/{doctorId}/schedules")
    public ResponseEntity<DoctorScheduleResponse> create(@PathVariable UUID doctorId, @Valid @RequestBody DoctorScheduleCreateRequest request, UriComponentsBuilder uriBuilder) {
        var created = doctorScheduleService.create(doctorId, request);
        var location = uriBuilder.path("/api/doctors/{doctorId}/schedules/{id}").buildAndExpand(doctorId, created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{doctorId}/schedules")
    public ResponseEntity<List<DoctorScheduleSummaryResponse>> getByDoctor(@PathVariable UUID doctorId) {
        var schedules = doctorScheduleService.findByDoctor(doctorId);
        return ResponseEntity.ok(schedules);
    }

}
