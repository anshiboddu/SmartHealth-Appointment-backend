package com.healthappointment.service;

import com.healthappointment.dto.DoctorCreateRequest;
import com.healthappointment.dto.DoctorDto;
import com.healthappointment.dto.DoctorUpdateRequest;
import com.healthappointment.entity.Department;
import com.healthappointment.entity.DoctorProfile;
import com.healthappointment.entity.User;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.DepartmentRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorProfileRepository doctorProfileRepository,
                         UserRepository userRepository,
                         DepartmentRepository departmentRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getActiveDoctors() {
        return doctorProfileRepository.findAllActiveDoctors()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        DoctorProfile doctor = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with id: " + id));
        return mapToDto(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getDoctorsByDepartment(Long departmentId) {
        return doctorProfileRepository.findByDepartmentIdAndActiveTrue(departmentId)
                .stream()
                .filter(d -> d.getUser().isActive())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDto createDoctor(DoctorCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().toLowerCase().trim(),
                request.getPhone().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.DOCTOR
        );
        User savedUser = userRepository.save(user);

        DoctorProfile profile = new DoctorProfile(
                savedUser,
                department,
                request.getSpecialization().trim(),
                request.getQualification().trim(),
                request.getExperienceYears(),
                request.getConsultationFee(),
                request.getBio() != null ? request.getBio().trim() : null
        );

        DoctorProfile savedProfile = doctorProfileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    @Transactional
    public DoctorDto updateDoctor(Long id, DoctorUpdateRequest request) {
        DoctorProfile doctor = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with id: " + id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        User user = doctor.getUser();
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
            doctor.setActive(request.getActive());
        }
        userRepository.save(user);

        doctor.setDepartment(department);
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setQualification(request.getQualification().trim());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBio(request.getBio() != null ? request.getBio().trim() : null);

        DoctorProfile updated = doctorProfileRepository.save(doctor);
        return mapToDto(updated);
    }

    @Transactional
    public DoctorDto toggleDoctorStatus(Long id) {
        DoctorProfile doctor = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with id: " + id));

        boolean newStatus = !doctor.isActive();
        doctor.setActive(newStatus);
        doctor.getUser().setActive(newStatus);
        userRepository.save(doctor.getUser());

        DoctorProfile updated = doctorProfileRepository.save(doctor);
        return mapToDto(updated);
    }

    public DoctorDto mapToDto(DoctorProfile profile) {
        return new DoctorDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getUser().getPhone(),
                profile.getDepartment().getId(),
                profile.getDepartment().getName(),
                profile.getSpecialization(),
                profile.getQualification(),
                profile.getExperienceYears(),
                profile.getConsultationFee(),
                profile.getBio(),
                profile.isActive() && profile.getUser().isActive()
        );
    }
}
