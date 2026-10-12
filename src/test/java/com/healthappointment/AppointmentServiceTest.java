package com.healthappointment;

import com.healthappointment.dto.AppointmentBookingRequest;
import com.healthappointment.dto.AppointmentDto;
import com.healthappointment.dto.AppointmentStatusUpdateRequest;
import com.healthappointment.entity.*;
import com.healthappointment.enums.AppointmentStatus;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.exception.ForbiddenException;
import com.healthappointment.repository.*;
import com.healthappointment.security.UserPrincipal;
import com.healthappointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    private AppointmentService appointmentService;

    private User patient;
    private User doctorUser;
    private Department department;
    private DoctorProfile doctor;
    private UserPrincipal patientPrincipal;
    private UserPrincipal doctorPrincipal;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                userRepository,
                doctorProfileRepository,
                departmentRepository,
                availabilityRepository,
                consultationRepository
        );

        patient = new User("John Doe", "john@example.com", "1234567890", "pass", Role.PATIENT);
        patient.setId(1L);

        doctorUser = new User("Dr. Smith", "drsmith@example.com", "9876543210", "pass", Role.DOCTOR);
        doctorUser.setId(2L);

        department = new Department("General Medicine", "Primary care");
        department.setId(10L);

        doctor = new DoctorProfile(doctorUser, department, "Internal Medicine", "MD", 10, 50.0, "Bio");
        doctor.setId(20L);

        patientPrincipal = new UserPrincipal(1L, "John Doe", "john@example.com", "pass", Role.PATIENT, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")));

        doctorPrincipal = new UserPrincipal(2L, "Dr. Smith", "drsmith@example.com", "pass", Role.DOCTOR, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR")));
    }

    @Test
    void testBookAppointmentSuccess() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        AppointmentBookingRequest request = new AppointmentBookingRequest(
                20L, 10L, futureDate, startTime, endTime, "Fever", "Need checkup"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        DoctorAvailability availability = new DoctorAvailability(doctor, futureDate, LocalTime.of(9, 0), LocalTime.of(12, 0));
        when(availabilityRepository.findByDoctorIdAndAppointmentDateAndAvailableTrueOrderByStartTimeAsc(20L, futureDate))
                .thenReturn(List.of(availability));

        when(appointmentRepository.existsDoctorOverlappingAppointment(eq(20L), eq(futureDate), eq(startTime), eq(endTime), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.existsPatientOverlappingAppointment(eq(1L), eq(futureDate), eq(startTime), eq(endTime), any(), any()))
                .thenReturn(false);

        Appointment savedApt = new Appointment(patient, doctor, department, futureDate, startTime, endTime, "Fever", "Need checkup");
        savedApt.setId(100L);
        savedApt.setStatus(AppointmentStatus.PENDING);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedApt);

        AppointmentDto dto = appointmentService.bookAppointment(patientPrincipal, request);

        assertNotNull(dto);
        assertEquals(AppointmentStatus.PENDING, dto.getStatus());
        assertEquals("John Doe", dto.getPatientName());
        assertEquals(100L, dto.getId());
    }

    @Test
    void testBookAppointmentDoubleBookingThrowsConflict() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        AppointmentBookingRequest request = new AppointmentBookingRequest(
                20L, 10L, futureDate, startTime, endTime, "Fever", "Need checkup"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        DoctorAvailability availability = new DoctorAvailability(doctor, futureDate, LocalTime.of(9, 0), LocalTime.of(12, 0));
        when(availabilityRepository.findByDoctorIdAndAppointmentDateAndAvailableTrueOrderByStartTimeAsc(20L, futureDate))
                .thenReturn(List.of(availability));

        // Slot already booked by someone else!
        when(appointmentRepository.existsDoctorOverlappingAppointment(eq(20L), eq(futureDate), eq(startTime), eq(endTime), any(), any()))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(patientPrincipal, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void testBookAppointmentOutsideDoctorAvailabilityThrowsBadRequest() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        AppointmentBookingRequest request = new AppointmentBookingRequest(
                20L, 10L, futureDate, startTime, endTime, "Fever", "Need checkup"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        // No availability configured for this day
        when(availabilityRepository.findByDoctorIdAndAppointmentDateAndAvailableTrueOrderByStartTimeAsc(20L, futureDate))
                .thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(patientPrincipal, request));
    }

    @Test
    void testPreventPatientFromViewingAnotherPatientsAppointment() {
        User otherPatient = new User("Other Patient", "other@example.com", "1112223334", "pass", Role.PATIENT);
        otherPatient.setId(99L);

        Appointment appointment = new Appointment(otherPatient, doctor, department, LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Cough", null);
        appointment.setId(500L);

        when(appointmentRepository.findById(500L)).thenReturn(Optional.of(appointment));

        // Patient 1 tries to access Patient 99's appointment
        assertThrows(ForbiddenException.class, () -> appointmentService.getAppointmentById(500L, patientPrincipal));
    }

    @Test
    void testPreventDoctorFromUpdatingAnotherDoctorsAppointment() {
        DoctorProfile otherDoctor = new DoctorProfile(new User(), department, "Surgeon", "MS", 8, 90.0, "Bio");
        otherDoctor.setId(999L);

        Appointment appointment = new Appointment(patient, otherDoctor, department, LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Cough", null);
        appointment.setId(501L);

        when(doctorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(doctor)); // doctor is id 20L
        when(appointmentRepository.findById(501L)).thenReturn(Optional.of(appointment));

        AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest(AppointmentStatus.CONFIRMED);

        // Doctor 20L tries to manage Doctor 999L's appointment
        assertThrows(ForbiddenException.class, () -> appointmentService.updateDoctorAppointmentStatus(501L, doctorPrincipal, request));
    }

    @Test
    void testCompletedAppointmentCannotBeCancelled() {
        Appointment completedApt = new Appointment(patient, doctor, department, LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Cough", null);
        completedApt.setId(600L);
        completedApt.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(600L)).thenReturn(Optional.of(completedApt));

        assertThrows(BadRequestException.class, () -> appointmentService.cancelAppointment(600L, patientPrincipal));
    }
}
