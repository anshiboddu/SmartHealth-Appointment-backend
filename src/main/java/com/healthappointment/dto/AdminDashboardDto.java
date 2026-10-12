package com.healthappointment.dto;

import java.util.List;

public class AdminDashboardDto {

    private long totalPatients;
    private long totalDoctors;
    private long totalDepartments;
    private long totalAppointments;

    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long rejectedAppointments;

    private List<AppointmentDto> recentAppointments;

    public AdminDashboardDto() {
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(long pendingAppointments) {
        this.pendingAppointments = pendingAppointments;
    }

    public long getConfirmedAppointments() {
        return confirmedAppointments;
    }

    public void setConfirmedAppointments(long confirmedAppointments) {
        this.confirmedAppointments = confirmedAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public long getRejectedAppointments() {
        return rejectedAppointments;
    }

    public void setRejectedAppointments(long rejectedAppointments) {
        this.rejectedAppointments = rejectedAppointments;
    }

    public List<AppointmentDto> getRecentAppointments() {
        return recentAppointments;
    }

    public void setRecentAppointments(List<AppointmentDto> recentAppointments) {
        this.recentAppointments = recentAppointments;
    }
}
