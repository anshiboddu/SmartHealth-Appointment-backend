package com.healthappointment.controller;

import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.DepartmentDto;
import com.healthappointment.dto.DepartmentRequest;
import com.healthappointment.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartments(
            @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        List<DepartmentDto> departments = activeOnly
                ? departmentService.getActiveDepartments()
                : departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentById(@PathVariable Long id) {
        DepartmentDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentDto created = departmentService.createDepartment(request);
        return new ResponseEntity<>(
                ApiResponse.success("Department created successfully", created),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentDto updated = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DepartmentDto>> toggleDepartmentStatus(@PathVariable Long id) {
        DepartmentDto updated = departmentService.toggleDepartmentStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Department status updated successfully", updated));
    }
}
