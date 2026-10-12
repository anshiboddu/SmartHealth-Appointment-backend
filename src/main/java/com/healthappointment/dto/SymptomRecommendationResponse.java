package com.healthappointment.dto;

import java.util.List;

public class SymptomRecommendationResponse {

    private List<DepartmentRecommendationDto> recommendations;
    private List<DoctorDto> availableDoctors;
    private String disclaimer = "This tool provides general department suggestions based on selected symptoms. It is not a medical diagnosis and does not replace professional medical advice.";
    private String emergencyWarning = "EMERGENCY WARNING: If you are experiencing severe or potentially life-threatening symptoms (such as chest pain, severe difficulty breathing, sudden weakness or numbness, loss of consciousness, or severe bleeding), seek immediate emergency medical care immediately. Do not rely on this application for urgent medical conditions.";

    public SymptomRecommendationResponse() {
    }

    public SymptomRecommendationResponse(List<DepartmentRecommendationDto> recommendations, List<DoctorDto> availableDoctors) {
        this.recommendations = recommendations;
        this.availableDoctors = availableDoctors;
    }

    public List<DepartmentRecommendationDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<DepartmentRecommendationDto> recommendations) {
        this.recommendations = recommendations;
    }

    public List<DoctorDto> getAvailableDoctors() {
        return availableDoctors;
    }

    public void setAvailableDoctors(List<DoctorDto> availableDoctors) {
        this.availableDoctors = availableDoctors;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getEmergencyWarning() {
        return emergencyWarning;
    }

    public void setEmergencyWarning(String emergencyWarning) {
        this.emergencyWarning = emergencyWarning;
    }
}
