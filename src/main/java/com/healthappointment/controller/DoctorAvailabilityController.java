package com.healthappointment.controller;

import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.AvailabilityDto;
import com.healthappointment.dto.AvailabilityRequest;
import com.healthappointment.security.UserPrincipal;
import com.healthappointment.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor/availability")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailabilityDto>>> getMyAvailability(
            @AuthenticationPrincipal UserPrincipal doctorUser) {
        List<AvailabilityDto> list = availabilityService.getMyAvailability(doctorUser);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AvailabilityDto>> createAvailability(
            @AuthenticationPrincipal UserPrincipal doctorUser,
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityDto created = availabilityService.createAvailability(doctorUser, request);
        return new ResponseEntity<>(
                ApiResponse.success("Availability slot created successfully", created),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailabilityDto>> updateAvailability(
            @AuthenticationPrincipal UserPrincipal doctorUser,
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityDto updated = availabilityService.updateAvailability(doctorUser, id, request);
        return ResponseEntity.ok(ApiResponse.success("Availability slot updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @AuthenticationPrincipal UserPrincipal doctorUser,
            @PathVariable Long id) {
        availabilityService.deleteAvailability(doctorUser, id);
        return ResponseEntity.ok(ApiResponse.success("Availability slot removed successfully", null));
    }
}
