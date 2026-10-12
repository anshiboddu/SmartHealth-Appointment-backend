package com.healthappointment.service;

import com.healthappointment.dto.DepartmentRecommendationDto;
import com.healthappointment.dto.DoctorDto;
import com.healthappointment.dto.SymptomRecommendationRequest;
import com.healthappointment.dto.SymptomRecommendationResponse;
import com.healthappointment.entity.Department;
import com.healthappointment.entity.DoctorProfile;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.repository.DepartmentRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SymptomService {

    private final DepartmentRepository departmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorService doctorService;

    // Centralized symptom-to-department rule mapping
    private static final Map<String, String> SYMPTOM_DEPARTMENT_MAP = new LinkedHashMap<>();
    private static final Map<String, String> DEPARTMENT_EXPLANATION_MAP = new HashMap<>();

    static {
        // Required mappings
        SYMPTOM_DEPARTMENT_MAP.put("Acne or skin rash", "Dermatology");
        SYMPTOM_DEPARTMENT_MAP.put("Fever", "General Medicine");
        SYMPTOM_DEPARTMENT_MAP.put("Cough", "General Medicine");
        SYMPTOM_DEPARTMENT_MAP.put("Toothache", "Dentistry");
        SYMPTOM_DEPARTMENT_MAP.put("Eye irritation", "Ophthalmology");
        SYMPTOM_DEPARTMENT_MAP.put("Joint pain", "Orthopedics");
        SYMPTOM_DEPARTMENT_MAP.put("Back pain", "Orthopedics");
        SYMPTOM_DEPARTMENT_MAP.put("Headache", "General Medicine");
        SYMPTOM_DEPARTMENT_MAP.put("Dizziness", "General Medicine");

        // Explanations for each department
        DEPARTMENT_EXPLANATION_MAP.put("Dermatology", "Specializes in conditions affecting skin, hair, and nails.");
        DEPARTMENT_EXPLANATION_MAP.put("General Medicine", "Specializes in comprehensive primary care, acute common illnesses, fevers, and internal medicine.");
        DEPARTMENT_EXPLANATION_MAP.put("Dentistry", "Specializes in oral health, teeth, gums, and related structures.");
        DEPARTMENT_EXPLANATION_MAP.put("Ophthalmology", "Specializes in eye health, vision problems, and ocular conditions.");
        DEPARTMENT_EXPLANATION_MAP.put("Orthopedics", "Specializes in musculoskeletal conditions, joints, bones, and back spine issues.");
    }

    public SymptomService(DepartmentRepository departmentRepository,
                          DoctorProfileRepository doctorProfileRepository,
                          DoctorService doctorService) {
        this.departmentRepository = departmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorService = doctorService;
    }

    public List<String> getAllSupportedSymptoms() {
        return new ArrayList<>(SYMPTOM_DEPARTMENT_MAP.keySet());
    }

    @Transactional(readOnly = true)
    public SymptomRecommendationResponse recommendDepartments(SymptomRecommendationRequest request) {
        if (request.getSymptoms() == null || request.getSymptoms().isEmpty()) {
            throw new BadRequestException("Please select at least one symptom.");
        }

        // Map department name -> matched symptoms
        Map<String, List<String>> departmentToMatchedSymptoms = new LinkedHashMap<>();

        for (String rawSymptom : request.getSymptoms()) {
            if (rawSymptom == null || rawSymptom.trim().isEmpty()) {
                continue;
            }
            String symptom = rawSymptom.trim();

            // Match case-insensitively or exact
            String matchedDeptName = null;
            for (Map.Entry<String, String> entry : SYMPTOM_DEPARTMENT_MAP.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(symptom)) {
                    matchedDeptName = entry.getValue();
                    break;
                }
            }

            if (matchedDeptName != null) {
                departmentToMatchedSymptoms
                        .computeIfAbsent(matchedDeptName, k -> new ArrayList<>())
                        .add(symptom);
            }
        }

        List<DepartmentRecommendationDto> recommendationDtos = new ArrayList<>();
        Set<DoctorDto> availableDoctorSet = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> entry : departmentToMatchedSymptoms.entrySet()) {
            String deptName = entry.getKey();
            List<String> matchedSymptoms = entry.getValue();

            Optional<Department> optDept = departmentRepository.findByNameIgnoreCase(deptName);
            if (optDept.isPresent() && optDept.get().isActive()) {
                Department dept = optDept.get();
                String explanation = DEPARTMENT_EXPLANATION_MAP.getOrDefault(
                        dept.getName(), "Recommended department for the selected symptoms."
                );

                recommendationDtos.add(new DepartmentRecommendationDto(
                        dept.getId(),
                        dept.getName(),
                        dept.getDescription(),
                        explanation,
                        matchedSymptoms
                ));

                // Find active doctors in this department
                List<DoctorProfile> doctors = doctorProfileRepository.findByDepartmentIdAndActiveTrue(dept.getId());
                for (DoctorProfile doc : doctors) {
                    if (doc.getUser().isActive()) {
                        availableDoctorSet.add(doctorService.mapToDto(doc));
                    }
                }
            }
        }

        return new SymptomRecommendationResponse(
                recommendationDtos,
                new ArrayList<>(availableDoctorSet)
        );
    }
}
