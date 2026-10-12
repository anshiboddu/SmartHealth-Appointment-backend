package com.healthappointment.controller;

import com.healthappointment.dto.AdminDashboardDto;
import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.UserSummaryDto;
import com.healthappointment.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboardStats() {
        AdminDashboardDto stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/patients")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getAllPatients() {
        List<UserSummaryDto> patients = adminDashboardService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @PatchMapping("/patients/{id}/status")
    public ResponseEntity<ApiResponse<UserSummaryDto>> togglePatientStatus(@PathVariable Long id) {
        UserSummaryDto updated = adminDashboardService.togglePatientStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Patient status updated successfully", updated));
    }
}
