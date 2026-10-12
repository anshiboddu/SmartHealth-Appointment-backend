package com.healthappointment.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SymptomRecommendationRequest {

    @NotEmpty(message = "Please select at least one symptom")
    private List<String> symptoms;

    public SymptomRecommendationRequest() {
    }

    public SymptomRecommendationRequest(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}
