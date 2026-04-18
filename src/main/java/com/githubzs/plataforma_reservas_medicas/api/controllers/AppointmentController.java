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

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCancelRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCompleteRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentCreateRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentResponse;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSearchRequest;
import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.AppointmentSummaryResponse;
import com.githubzs.plataforma_reservas_medicas.services.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Validated
public class AppointmentController {

    private final AppointmentService appointmentService;
 
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request, UriComponentsBuilder uriBuilder) {
       
        var appointmentCreated= appointmentService.create(request);

        var location = uriBuilder.path("/api/appointments/{id}").buildAndExpand(appointmentCreated.id()).toUri();

        return ResponseEntity.created(location).body(appointmentCreated);    
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<AppointmentResponse> getById(@PathVariable UUID id) {

       var appointment = appointmentService.findById(id);

       return ResponseEntity.ok(appointment);
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentSummaryResponse>> list(@Valid @RequestBody AppointmentSearchRequest request, @RequestParam (defaultValue = "0") int page, @RequestParam (defaultValue = "10") int size) {
        
       var appointment = appointmentService.findAll(request,PageRequest.of(page, size));

       return ResponseEntity.ok(appointment);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentSummaryResponse> patchConfirm(@PathVariable UUID id) {
          return ResponseEntity.ok(appointmentService.confirm(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentSummaryResponse> patchCancel(@PathVariable UUID id, @Valid @RequestBody AppointmentCancelRequest request) {
          return ResponseEntity.ok(appointmentService.cancel(id, request));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentSummaryResponse> patchComplete(@PathVariable UUID id, @Valid @RequestBody AppointmentCompleteRequest request) {
          return ResponseEntity.ok(appointmentService.complete(id, request));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentSummaryResponse> patchNoShow(@PathVariable UUID id) {
          return ResponseEntity.ok(appointmentService.markNoShow(id));
    }


    
}
    

