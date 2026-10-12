package com.healthappointment.controller;

import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.AppointmentBookingRequest;
import com.healthappointment.dto.AppointmentDto;
import com.healthappointment.dto.AppointmentStatusUpdateRequest;
import com.healthappointment.enums.AppointmentStatus;
import com.healthappointment.security.UserPrincipal;
import com.healthappointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Patient: Book appointment
    @PostMapping("/api/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> bookAppointment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AppointmentBookingRequest request) {
        AppointmentDto booked = appointmentService.bookAppointment(userPrincipal, request);
        return new ResponseEntity<>(
                ApiResponse.success("Appointment booked successfully with status PENDING", booked),
                HttpStatus.CREATED
        );
    }

    // Patient: View my appointments
    @GetMapping("/api/appointments/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getMyAppointments(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<AppointmentDto> list = appointmentService.getMyAppointments(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // Patient / Doctor / Admin: View specific appointment
    @GetMapping("/api/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentDto>> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AppointmentDto dto = appointmentService.getAppointmentById(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // Patient: Cancel appointment
    @PatchMapping("/api/appointments/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        AppointmentDto dto = appointmentService.cancelAppointment(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", dto));
    }

    // Doctor: View appointments assigned to this doctor
    @GetMapping("/api/doctor/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getDoctorAppointments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) AppointmentStatus status) {
        List<AppointmentDto> list = appointmentService.getDoctorAppointments(userPrincipal, date, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // Doctor: Update appointment status (accept, reject, complete)
    @PatchMapping("/api/doctor/appointments/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentDto>> updateAppointmentStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        AppointmentDto dto = appointmentService.updateDoctorAppointmentStatus(id, userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated to " + dto.getStatus(), dto));
    }

    // Admin: View all appointments with filters
    @GetMapping("/api/admin/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getAllAppointmentsAdmin(
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) AppointmentStatus status) {
        List<AppointmentDto> list = appointmentService.getAllAppointmentsAdmin(doctorId, departmentId, date, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
