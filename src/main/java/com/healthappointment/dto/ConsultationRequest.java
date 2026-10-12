package com.healthappointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ConsultationRequest {

    @NotBlank(message = "Doctor notes are required")
    @Size(min = 5, max = 3000, message = "Doctor notes must be between 5 and 3000 characters")
    private String doctorNotes;

    @Size(max = 3000, message = "Prescription instructions cannot exceed 3000 characters")
    private String prescriptionInstructions;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate followUpDate;

    public ConsultationRequest() {
    }

    public ConsultationRequest(String doctorNotes, String prescriptionInstructions, LocalDate followUpDate) {
        this.doctorNotes = doctorNotes;
        this.prescriptionInstructions = prescriptionInstructions;
        this.followUpDate = followUpDate;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public String getPrescriptionInstructions() {
        return prescriptionInstructions;
    }

    public void setPrescriptionInstructions(String prescriptionInstructions) {
        this.prescriptionInstructions = prescriptionInstructions;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }
}
