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

import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.DoctorDtos.DoctorUpdateRequest;
import com.githubzs.plataforma_reservas_medicas.services.DoctorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

	private final DoctorService doctorService;

	@PostMapping
	public ResponseEntity<DoctorResponse> create(@Valid @RequestBody DoctorCreateRequest request, UriComponentsBuilder uriBuilder) {
		var doctorCreated = doctorService.create(request);
		var location = uriBuilder.path("/api/doctors/{id}").buildAndExpand(doctorCreated.id()).toUri();
		return ResponseEntity.created(location).body(doctorCreated);
	}

	@GetMapping("/{id}")
	public ResponseEntity<DoctorSummaryResponse> get(@PathVariable UUID id) {
		return ResponseEntity.ok(doctorService.findById(id));
	}

	@GetMapping
	public ResponseEntity<Page<DoctorSummaryResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		var result = doctorService.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending()));
		return ResponseEntity.ok(result);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<DoctorSummaryResponse> update(@PathVariable UUID id, @Valid @RequestBody DoctorUpdateRequest request) {
		return ResponseEntity.ok(doctorService.update(id, request));
	}

}
