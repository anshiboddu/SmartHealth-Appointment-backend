package com.healthappointment.service;

import com.healthappointment.dto.DepartmentDto;
import com.healthappointment.dto.DepartmentRequest;
import com.healthappointment.entity.Department;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.DepartmentRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             DoctorProfileRepository doctorProfileRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getActiveDepartments() {
        return departmentRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDto(department);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ConflictException("Department already exists with name: " + request.getName());
        }

        Department department = new Department(
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null
        );
        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }

        Department saved = departmentRepository.save(department);
        return mapToDto(saved);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // If name changed, check uniqueness
        if (!department.getName().equalsIgnoreCase(request.getName().trim()) &&
                departmentRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ConflictException("Another department already exists with name: " + request.getName());
        }

        department.setName(request.getName().trim());
        department.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }

        Department updated = departmentRepository.save(department);
        return mapToDto(updated);
    }

    @Transactional
    public DepartmentDto toggleDepartmentStatus(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        department.setActive(!department.isActive());
        Department updated = departmentRepository.save(department);
        return mapToDto(updated);
    }

    public DepartmentDto mapToDto(Department department) {
        long doctorCount = doctorProfileRepository.countByDepartmentId(department.getId());
        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.isActive(),
                doctorCount,
                department.getCreatedAt()
        );
    }
}
