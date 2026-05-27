package com.githubzs.plataforma_reservas_medicas.api.controllers;

import java.util.UUID;
import java.security.Principal;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;

import com.githubzs.plataforma_reservas_medicas.api.dto.AppointmentDtos.*;
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
       return ResponseEntity.ok(appointmentService.findById(id));
    }

      @GetMapping("/mine")
      public ResponseEntity<Page<AppointmentSummaryResponse>> listMine(
                  Principal principal,
                  @RequestParam(defaultValue = "0") int page,
                  @RequestParam(defaultValue = "10") int size) {
            var documentNumber = principal != null ? principal.getName() : null;
            var result = appointmentService.findByDoctorDocumentNumber(
                  documentNumber,
                  PageRequest.of(page, size, Sort.by("createdAt").ascending())
            );
            return ResponseEntity.ok(result);
      }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> list(@Valid @ModelAttribute AppointmentSearchRequest request, @RequestParam (defaultValue = "0") int page, @RequestParam (defaultValue = "10") int size) {
       var result = appointmentService.findAll(request,PageRequest.of(page, size, Sort.by("createdAt").ascending()));
       return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentStatusUpdateResponse> patchConfirm(@PathVariable UUID id) {
          return ResponseEntity.ok(appointmentService.confirm(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentStatusUpdateResponse> patchCancel(@PathVariable UUID id, @Valid @RequestBody AppointmentCancelRequest request) {
          return ResponseEntity.ok(appointmentService.cancel(id, request));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentStatusUpdateResponse> patchComplete(@PathVariable UUID id, @Valid @RequestBody AppointmentCompleteRequest request) {
          return ResponseEntity.ok(appointmentService.complete(id, request));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentStatusUpdateResponse> patchNoShow(@PathVariable UUID id) {
          return ResponseEntity.ok(appointmentService.markNoShow(id));
    }

}
    

