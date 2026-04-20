package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.services.AppointmentTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
@Validated
public class AppointmentTypeController {

    private final AppointmentTypeService appointmentTypeService;

    @PostMapping
    public ResponseEntity<AppointmentTypeResponse> create(@Valid @RequestBody AppointmentTypeCreateRequest request, UriComponentsBuilder uriBuilder) {
        var appointmentTypeCreated = appointmentTypeService.create(request);
        var location = uriBuilder.path("/api/appointment-types").build().toUri();
        return ResponseEntity.created(location).body(appointmentTypeCreated);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentTypeSummaryResponse>> list() {
        return ResponseEntity.ok(appointmentTypeService.findAll());
    }

}
