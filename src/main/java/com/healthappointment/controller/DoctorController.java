package com.healthappointment.controller;

import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.AvailabilityDto;
import com.healthappointment.dto.DoctorCreateRequest;
import com.healthappointment.dto.DoctorDto;
import com.healthappointment.dto.DoctorUpdateRequest;
import com.healthappointment.service.DoctorAvailabilityService;
import com.healthappointment.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorAvailabilityService availabilityService;

    public DoctorController(DoctorService doctorService, DoctorAvailabilityService availabilityService) {
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getDoctors(
            @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        List<DoctorDto> doctors = activeOnly ? doctorService.getActiveDoctors() : doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(@PathVariable Long id) {
        DoctorDto doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(doctor));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<DoctorDto> doctors = doctorService.getDoctorsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityDto>>> getDoctorAvailability(@PathVariable Long id) {
        List<AvailabilityDto> availability = availabilityService.getAvailabilityForDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(availability));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorDto>> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        DoctorDto created = doctorService.createDoctor(request);
        return new ResponseEntity<>(
                ApiResponse.success("Doctor created successfully", created),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDto>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request) {
        DoctorDto updated = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DoctorDto>> toggleDoctorStatus(@PathVariable Long id) {
        DoctorDto updated = doctorService.toggleDoctorStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor status updated successfully", updated));
    }
}
