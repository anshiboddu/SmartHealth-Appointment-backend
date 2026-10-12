package com.healthappointment.controller;

import com.healthappointment.dto.ApiResponse;
import com.healthappointment.dto.SymptomRecommendationRequest;
import com.healthappointment.dto.SymptomRecommendationResponse;
import com.healthappointment.service.SymptomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
public class SymptomController {

    private final SymptomService symptomService;

    public SymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableSymptoms() {
        List<String> symptoms = symptomService.getAllSupportedSymptoms();
        return ResponseEntity.ok(ApiResponse.success(symptoms));
    }

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<SymptomRecommendationResponse>> recommendDepartments(
            @Valid @RequestBody SymptomRecommendationRequest request) {
        SymptomRecommendationResponse response = symptomService.recommendDepartments(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
