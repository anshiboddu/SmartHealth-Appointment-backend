package com.healthappointment.service;

import com.healthappointment.dto.AppointmentBookingRequest;
import com.healthappointment.dto.AppointmentDto;
import com.healthappointment.dto.AppointmentStatusUpdateRequest;
import com.healthappointment.dto.ConsultationDto;
import com.healthappointment.entity.*;
import com.healthappointment.enums.AppointmentStatus;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.exception.ForbiddenException;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.*;
import com.healthappointment.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final ConsultationRepository consultationRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              DoctorProfileRepository doctorProfileRepository,
                              DepartmentRepository departmentRepository,
                              DoctorAvailabilityRepository availabilityRepository,
                              ConsultationRepository consultationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.departmentRepository = departmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.consultationRepository = consultationRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AppointmentDto bookAppointment(UserPrincipal patientPrincipal, AppointmentBookingRequest request) {
        // 1. Verify Patient
        User patient = userRepository.findById(patientPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient account not found"));

        if (!patient.isActive()) {
            throw new BadRequestException("Your patient account is inactive");
        }

        // 2. Validate Date and Times
        LocalDate today = LocalDate.now();
        if (request.getAppointmentDate().isBefore(today)) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // If booking for today, time must not be past
        if (request.getAppointmentDate().isEqual(today) && request.getStartTime().isBefore(LocalTime.now())) {
            throw new BadRequestException("Appointment time has already passed for today");
        }

        // 3. Verify Doctor and Department
        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        if (!doctor.isActive() || !doctor.getUser().isActive()) {
            throw new BadRequestException("The requested doctor is currently inactive");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (!department.isActive()) {
            throw new BadRequestException("The selected department is inactive");
        }

        if (!doctor.getDepartment().getId().equals(department.getId())) {
            throw new BadRequestException("Selected doctor does not belong to the selected department");
        }

        // 4. Validate doctor availability slot
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndAppointmentDateAndAvailableTrueOrderByStartTimeAsc(
                        doctor.getId(), request.getAppointmentDate()
                );

        boolean slotMatchesAvailability = availabilities.stream().anyMatch(a ->
                !request.getStartTime().isBefore(a.getStartTime()) && !request.getEndTime().isAfter(a.getEndTime())
        );

        if (!slotMatchesAvailability) {
            throw new BadRequestException("Doctor has not published active availability for this date and time window");
        }

        // 5. Check if Doctor already has an overlapping active appointment
        List<AppointmentStatus> activeStatuses = Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

        boolean doctorOverlap = appointmentRepository.existsDoctorOverlappingAppointment(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                activeStatuses,
                null
        );

        if (doctorOverlap) {
            throw new ConflictException("The selected time slot is already booked for Dr. " + doctor.getUser().getFullName() + ". Please choose another slot.");
        }

        // 6. Check if Patient already has an overlapping active appointment
        boolean patientOverlap = appointmentRepository.existsPatientOverlappingAppointment(
                patient.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                activeStatuses,
                null
        );

        if (patientOverlap) {
            throw new ConflictException("You already have an active appointment scheduled during this time window.");
        }

        // 7. Persist Appointment
        Appointment appointment = new Appointment(
                patient,
                doctor,
                department,
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getSymptoms() != null ? request.getSymptoms().trim() : null,
                request.getPatientNotes() != null ? request.getPatientNotes().trim() : null
        );
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return mapToDto(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointments(UserPrincipal patientPrincipal) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientPrincipal.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(UserPrincipal doctorPrincipal, LocalDate date, AppointmentStatus status) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Doctor profile not found for authenticated user"));

        List<Appointment> list = appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctor.getId());

        return list.stream()
                .filter(a -> date == null || a.getAppointmentDate().equals(date))
                .filter(a -> status == null || a.getStatus() == status)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointmentsAdmin(Long doctorId, Long departmentId, LocalDate date, AppointmentStatus status) {
        return appointmentRepository.findAllByOrderByAppointmentDateDescStartTimeDesc()
                .stream()
                .filter(a -> doctorId == null || a.getDoctor().getId().equals(doctorId))
                .filter(a -> departmentId == null || a.getDepartment().getId().equals(departmentId))
                .filter(a -> date == null || a.getAppointmentDate().equals(date))
                .filter(a -> status == null || a.getStatus() == status)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long id, UserPrincipal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        // Ownership authorization check
        if (principal.getRole() == Role.PATIENT) {
            if (!appointment.getPatient().getId().equals(principal.getId())) {
                throw new ForbiddenException("You are not authorized to view this appointment");
            }
        } else if (principal.getRole() == Role.DOCTOR) {
            DoctorProfile doctor = doctorProfileRepository.findByUserId(principal.getId())
                    .orElseThrow(() -> new ForbiddenException("Doctor profile not found"));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to view this appointment");
            }
        }
        // ADMIN can view all

        return mapToDto(appointment);
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long id, UserPrincipal patientPrincipal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        // Check ownership
        if (!appointment.getPatient().getId().equals(patientPrincipal.getId())) {
            throw new ForbiddenException("You are not authorized to cancel this appointment");
        }

        // Rule: Completed appointments must not be cancelled through ordinary cancellation workflow
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Completed appointments cannot be cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new BadRequestException("Rejected appointments cannot be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);
        return mapToDto(updated);
    }

    @Transactional
    public AppointmentDto updateDoctorAppointmentStatus(Long id, UserPrincipal doctorPrincipal, AppointmentStatusUpdateRequest request) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        // Doctor ownership check
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to manage appointments for another doctor");
        }

        AppointmentStatus targetStatus = request.getStatus();

        // Status transition rules
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Appointment is already completed and its status cannot be changed");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Patient has already cancelled this appointment");
        }

        appointment.setStatus(targetStatus);
        Appointment updated = appointmentRepository.save(appointment);
        return mapToDto(updated);
    }

    public AppointmentDto mapToDto(Appointment a) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(a.getId());
        dto.setPatientId(a.getPatient().getId());
        dto.setPatientName(a.getPatient().getFullName());
        dto.setPatientEmail(a.getPatient().getEmail());
        dto.setPatientPhone(a.getPatient().getPhone());

        dto.setDoctorId(a.getDoctor().getId());
        dto.setDoctorName(a.getDoctor().getUser().getFullName());
        dto.setDoctorSpecialization(a.getDoctor().getSpecialization());
        dto.setConsultationFee(a.getDoctor().getConsultationFee());

        dto.setDepartmentId(a.getDepartment().getId());
        dto.setDepartmentName(a.getDepartment().getName());

        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setSymptoms(a.getSymptoms());
        dto.setPatientNotes(a.getPatientNotes());
        dto.setStatus(a.getStatus());
        dto.setCreatedAt(a.getCreatedAt());

        Optional<Consultation> consultation = consultationRepository.findByAppointmentId(a.getId());
        consultation.ifPresent(c -> dto.setConsultation(new ConsultationDto(
                c.getId(),
                c.getAppointment().getId(),
                c.getDoctorNotes(),
                c.getPrescriptionInstructions(),
                c.getFollowUpDate(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        )));

        return dto;
    }
}
