package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.githubzs.plataforma_reservas_medicas.api.dto.DashboardDtos.DashboardResponse;
import com.githubzs.plataforma_reservas_medicas.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Validated
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping
	public ResponseEntity<DashboardResponse> getDashboardStats(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		var result = dashboardService.getDashboardStats(date);
		return ResponseEntity.ok(result);
	}

}