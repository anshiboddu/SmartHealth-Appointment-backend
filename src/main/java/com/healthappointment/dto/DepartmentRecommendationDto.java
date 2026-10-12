package com.healthappointment.dto;

import java.util.List;

public class DepartmentRecommendationDto {

    private Long departmentId;
    private String departmentName;
    private String description;
    private String explanation;
    private List<String> matchedSymptoms;

    public DepartmentRecommendationDto() {
    }

    public DepartmentRecommendationDto(Long departmentId, String departmentName, String description,
                                       String explanation, List<String> matchedSymptoms) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.description = description;
        this.explanation = explanation;
        this.matchedSymptoms = matchedSymptoms;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getMatchedSymptoms() {
        return matchedSymptoms;
    }

    public void setMatchedSymptoms(List<String> matchedSymptoms) {
        this.matchedSymptoms = matchedSymptoms;
    }
}
