package com.healthappointment.service;

import com.healthappointment.dto.AdminDashboardDto;
import com.healthappointment.dto.AppointmentDto;
import com.healthappointment.dto.UserSummaryDto;
import com.healthappointment.entity.User;
import com.healthappointment.enums.AppointmentStatus;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.AppointmentRepository;
import com.healthappointment.repository.DepartmentRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public AdminDashboardService(UserRepository userRepository,
                                 DoctorProfileRepository doctorProfileRepository,
                                 DepartmentRepository departmentRepository,
                                 AppointmentRepository appointmentRepository,
                                 AppointmentService appointmentService) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        AdminDashboardDto dto = new AdminDashboardDto();

        dto.setTotalPatients(userRepository.countByRole(Role.PATIENT));
        dto.setTotalDoctors(doctorProfileRepository.countByActiveTrue());
        dto.setTotalDepartments(departmentRepository.countByActiveTrue());
        dto.setTotalAppointments(appointmentRepository.count());

        dto.setPendingAppointments(appointmentRepository.countByStatus(AppointmentStatus.PENDING));
        dto.setConfirmedAppointments(appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED));
        dto.setCompletedAppointments(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED));
        dto.setCancelledAppointments(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        dto.setRejectedAppointments(appointmentRepository.countByStatus(AppointmentStatus.REJECTED));

        List<AppointmentDto> recent = appointmentRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(appointmentService::mapToDto)
                .collect(Collectors.toList());
        dto.setRecentAppointments(recent);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAllPatients() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.PATIENT)
                .stream()
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getRole(),
                        u.isActive(),
                        u.getCreatedAt(),
                        null
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSummaryDto togglePatientStatus(Long patientId) {
        User user = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        user.setActive(!user.isActive());
        User updated = userRepository.save(user);

        return new UserSummaryDto(
                updated.getId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getPhone(),
                updated.getRole(),
                updated.isActive(),
                updated.getCreatedAt(),
                null
        );
    }
}
