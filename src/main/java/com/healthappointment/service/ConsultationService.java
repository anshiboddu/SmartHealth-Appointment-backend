package com.healthappointment.service;

import com.healthappointment.dto.ConsultationDto;
import com.healthappointment.dto.ConsultationRequest;
import com.healthappointment.entity.Appointment;
import com.healthappointment.entity.Consultation;
import com.healthappointment.entity.DoctorProfile;
import com.healthappointment.enums.AppointmentStatus;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.exception.ForbiddenException;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.AppointmentRepository;
import com.healthappointment.repository.ConsultationRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                               AppointmentRepository appointmentRepository,
                               DoctorProfileRepository doctorProfileRepository) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @Transactional
    public ConsultationDto createConsultation(Long appointmentId, UserPrincipal doctorPrincipal, ConsultationRequest request) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Ownership: Only the assigned doctor can add consultation notes/prescriptions
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new ForbiddenException("Only the doctor assigned to this appointment can create consultation notes");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new BadRequestException("Cannot create consultation for a cancelled or rejected appointment");
        }

        if (consultationRepository.existsByAppointmentId(appointmentId)) {
            throw new BadRequestException("Consultation record already exists for this appointment. Use update instead.");
        }

        Consultation consultation = new Consultation(
                appointment,
                request.getDoctorNotes().trim(),
                request.getPrescriptionInstructions() != null ? request.getPrescriptionInstructions().trim() : null,
                request.getFollowUpDate()
        );

        Consultation saved = consultationRepository.save(consultation);

        // Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return mapToDto(saved);
    }

    @Transactional
    public ConsultationDto updateConsultation(Long id, UserPrincipal doctorPrincipal, ConsultationRequest request) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Doctor profile not found"));

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        // Only the assigned doctor can update notes
        if (!consultation.getAppointment().getDoctor().getId().equals(doctor.getId())) {
            throw new ForbiddenException("Only the doctor assigned to this appointment can update consultation notes");
        }

        consultation.setDoctorNotes(request.getDoctorNotes().trim());
        consultation.setPrescriptionInstructions(
                request.getPrescriptionInstructions() != null ? request.getPrescriptionInstructions().trim() : null
        );
        consultation.setFollowUpDate(request.getFollowUpDate());

        Consultation updated = consultationRepository.save(consultation);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public ConsultationDto getConsultationByAppointmentId(Long appointmentId, UserPrincipal principal) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Authorization check
        if (principal.getRole() == Role.PATIENT) {
            if (!appointment.getPatient().getId().equals(principal.getId())) {
                throw new ForbiddenException("You are not authorized to view consultation details for this appointment");
            }
        } else if (principal.getRole() == Role.DOCTOR) {
            DoctorProfile doctor = doctorProfileRepository.findByUserId(principal.getId())
                    .orElseThrow(() -> new ForbiddenException("Doctor profile not found"));
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to view another doctor's consultation record");
            }
        }
        // ADMIN can view

        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No consultation record has been added yet for this appointment"));

        return mapToDto(consultation);
    }

    private ConsultationDto mapToDto(Consultation c) {
        return new ConsultationDto(
                c.getId(),
                c.getAppointment().getId(),
                c.getDoctorNotes(),
                c.getPrescriptionInstructions(),
                c.getFollowUpDate(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
