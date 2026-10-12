package com.healthappointment.dto;

import com.healthappointment.enums.Role;
import java.time.LocalDateTime;

public class UserSummaryDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private Long doctorProfileId;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String fullName, String email, String phone, Role role, boolean active, LocalDateTime createdAt, Long doctorProfileId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.doctorProfileId = doctorProfileId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDoctorProfileId() {
        return doctorProfileId;
    }

    public void setDoctorProfileId(Long doctorProfileId) {
        this.doctorProfileId = doctorProfileId;
    }
}
