package com.healthappointment.dto;

import com.healthappointment.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class AppointmentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    private String remarks;

    public AppointmentStatusUpdateRequest() {
    }

    public AppointmentStatusUpdateRequest(AppointmentStatus status) {
        this.status = status;
    }

    public AppointmentStatusUpdateRequest(AppointmentStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
