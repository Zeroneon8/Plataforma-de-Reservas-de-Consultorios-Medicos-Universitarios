package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.PatientDtos.PatientUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.services.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Validated
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientCreateRequest request, UriComponentsBuilder uriBuilder) {
        var patientCreated = patientService.create(request);
        var location = uriBuilder.path("/api/patients/{id}").buildAndExpand(patientCreated.id()).toUri();
        return ResponseEntity.created(location).body(patientCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientSummaryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PatientSummaryResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        var result = patientService.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable UUID id, @Valid @RequestBody PatientUpdateRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

}



