package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.OfficeDtos.OfficeUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.services.OfficeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
@Validated
public class OfficeController {

    private final OfficeService officeService;

    @PostMapping
    public ResponseEntity<OfficeResponse> create(@Valid @RequestBody OfficeCreateRequest request, UriComponentsBuilder uriBuilder) {
        var officeCreated = officeService.create(request);
        var location = uriBuilder.path("/api/offices/{id}").buildAndExpand(officeCreated.id()).toUri();
        return ResponseEntity.created(location).body(officeCreated);
    }

    @GetMapping
    public ResponseEntity<List<OfficeSummaryResponse>> list() {
        return ResponseEntity.ok(officeService.findAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OfficeResponse> update(@PathVariable UUID id, @Valid @RequestBody OfficeUpdateRequest request) {
        return ResponseEntity.ok(officeService.update(id, request));
    }

}

