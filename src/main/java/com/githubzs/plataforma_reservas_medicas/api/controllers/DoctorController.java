package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.githubzs.plataforma_reservas_medicas.service.DoctorService;

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
		var created = doctorService.create(request);
		var location = uriBuilder.path("/api/doctors/{id}").buildAndExpand(created.id()).toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<DoctorSummaryResponse> getById(@PathVariable UUID id) {
		var doctor = doctorService.findById(id);
		return ResponseEntity.ok(doctor);
	}

	@GetMapping
	public ResponseEntity<Page<DoctorSummaryResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		var doctors = doctorService.findAll(PageRequest.of(page, size));
		return ResponseEntity.ok(doctors);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<DoctorResponse> update(@PathVariable UUID id, @Valid @RequestBody DoctorUpdateRequest request) {
		var updated = doctorService.update(id, request);
		return ResponseEntity.ok(updated);
	}

}
