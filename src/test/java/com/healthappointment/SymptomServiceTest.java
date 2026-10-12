package com.healthappointment;

import com.healthappointment.dto.DoctorDto;
import com.healthappointment.dto.SymptomRecommendationRequest;
import com.healthappointment.dto.SymptomRecommendationResponse;
import com.healthappointment.entity.Department;
import com.healthappointment.entity.DoctorProfile;
import com.healthappointment.entity.User;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.repository.DepartmentRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.service.DoctorService;
import com.healthappointment.service.SymptomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private DoctorService doctorService;

    private SymptomService symptomService;

    @BeforeEach
    void setUp() {
        symptomService = new SymptomService(departmentRepository, doctorProfileRepository, doctorService);
    }

    @Test
    void testRecommendDepartmentForFeverAndCough() {
        Department genMed = new Department("General Medicine", "Primary Care");
        genMed.setId(1L);

        when(departmentRepository.findByNameIgnoreCase("General Medicine"))
                .thenReturn(Optional.of(genMed));
        when(doctorProfileRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(Collections.emptyList());

        SymptomRecommendationRequest request = new SymptomRecommendationRequest(
                Arrays.asList("Fever", "Cough")
        );

        SymptomRecommendationResponse response = symptomService.recommendDepartments(request);

        assertNotNull(response);
        assertEquals(1, response.getRecommendations().size());
        assertEquals("General Medicine", response.getRecommendations().get(0).getDepartmentName());
        assertTrue(response.getRecommendations().get(0).getMatchedSymptoms().contains("Fever"));
        assertTrue(response.getRecommendations().get(0).getMatchedSymptoms().contains("Cough"));
        assertNotNull(response.getDisclaimer());
        assertTrue(response.getDisclaimer().contains("not a medical diagnosis"));
        assertNotNull(response.getEmergencyWarning());
    }

    @Test
    void testMultipleDepartmentsRecommendation() {
        Department derm = new Department("Dermatology", "Skin Care");
        derm.setId(2L);
        Department ortho = new Department("Orthopedics", "Bone & Joint Care");
        ortho.setId(3L);

        when(departmentRepository.findByNameIgnoreCase("Dermatology")).thenReturn(Optional.of(derm));
        when(departmentRepository.findByNameIgnoreCase("Orthopedics")).thenReturn(Optional.of(ortho));
        when(doctorProfileRepository.findByDepartmentIdAndActiveTrue(2L)).thenReturn(Collections.emptyList());
        when(doctorProfileRepository.findByDepartmentIdAndActiveTrue(3L)).thenReturn(Collections.emptyList());

        SymptomRecommendationRequest request = new SymptomRecommendationRequest(
                Arrays.asList("Acne or skin rash", "Joint pain")
        );

        SymptomRecommendationResponse response = symptomService.recommendDepartments(request);

        assertEquals(2, response.getRecommendations().size());
    }

    @Test
    void testEmptySymptomsThrowsBadRequest() {
        SymptomRecommendationRequest request = new SymptomRecommendationRequest(Collections.emptyList());
        assertThrows(BadRequestException.class, () -> symptomService.recommendDepartments(request));
    }
}
