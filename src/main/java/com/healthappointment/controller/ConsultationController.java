package com.healthappointment.controller;

import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.ConsultationDto;
import com.healthappointment.dto.ConsultationRequest;
import com.healthappointment.security.UserPrincipal;
import com.healthappointment.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping("/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<ConsultationDto>> createConsultation(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal doctorUser,
            @Valid @RequestBody ConsultationRequest request) {
        ConsultationDto created = consultationService.createConsultation(appointmentId, doctorUser, request);
        return new ResponseEntity<>(
                ApiResponse.success("Consultation notes recorded successfully and appointment marked COMPLETED", created),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationDto>> getConsultationByAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ConsultationDto consultation = consultationService.getConsultationByAppointmentId(appointmentId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(consultation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<ConsultationDto>> updateConsultation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal doctorUser,
            @Valid @RequestBody ConsultationRequest request) {
        ConsultationDto updated = consultationService.updateConsultation(id, doctorUser, request);
        return ResponseEntity.ok(ApiResponse.success("Consultation notes updated successfully", updated));
    }
}
