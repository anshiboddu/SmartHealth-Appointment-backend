package com.healthappointment.service;

import com.healthappointment.dto.AvailabilityDto;
import com.healthappointment.dto.AvailabilityRequest;
import com.healthappointment.entity.DoctorAvailability;
import com.healthappointment.entity.DoctorProfile;
import com.healthappointment.enums.AppointmentStatus;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.exception.ForbiddenException;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.AppointmentRepository;
import com.healthappointment.repository.DoctorAvailabilityRepository;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorAvailabilityService(DoctorAvailabilityRepository availabilityRepository,
                                   DoctorProfileRepository doctorProfileRepository,
                                   AppointmentRepository appointmentRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> getAvailabilityForDoctor(Long doctorId) {
        return availabilityRepository.findByDoctorIdAndAppointmentDateGreaterThanEqualAndAvailableTrueOrderByAppointmentDateAscStartTimeAsc(
                        doctorId, LocalDate.now())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> getMyAvailability(UserPrincipal doctorUser) {
        DoctorProfile doctor = getDoctorByUserId(doctorUser.getId());
        return availabilityRepository.findByDoctorIdOrderByAppointmentDateAscStartTimeAsc(doctor.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityDto createAvailability(UserPrincipal doctorUser, AvailabilityRequest request) {
        DoctorProfile doctor = getDoctorByUserId(doctorUser.getId());

        validateSlotTimes(request);

        boolean overlap = availabilityRepository.existsOverlappingSlot(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                null
        );

        if (overlap) {
            throw new ConflictException("You already have an availability slot overlapping this time window on " + request.getAppointmentDate());
        }

        DoctorAvailability availability = new DoctorAvailability(
                doctor,
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        DoctorAvailability saved = availabilityRepository.save(availability);
        return mapToDto(saved);
    }

    @Transactional
    public AvailabilityDto updateAvailability(UserPrincipal doctorUser, Long id, AvailabilityRequest request) {
        DoctorProfile doctor = getDoctorByUserId(doctorUser.getId());

        DoctorAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found with id: " + id));

        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to update this availability slot");
        }

        validateSlotTimes(request);

        boolean overlap = availabilityRepository.existsOverlappingSlot(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                id
        );

        if (overlap) {
            throw new ConflictException("Another slot already overlaps this time window on " + request.getAppointmentDate());
        }

        availability.setAppointmentDate(request.getAppointmentDate());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());

        DoctorAvailability updated = availabilityRepository.save(availability);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteAvailability(UserPrincipal doctorUser, Long id) {
        DoctorProfile doctor = getDoctorByUserId(doctorUser.getId());

        DoctorAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found with id: " + id));

        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to delete this availability slot");
        }

        // Check if an active appointment already exists in this slot
        boolean hasBookedAppointment = appointmentRepository.existsDoctorOverlappingAppointment(
                doctor.getId(),
                availability.getAppointmentDate(),
                availability.getStartTime(),
                availability.getEndTime(),
                Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
                null
        );

        if (hasBookedAppointment) {
            throw new ConflictException("Cannot delete this slot because an active appointment is booked during this time");
        }

        availabilityRepository.delete(availability);
    }

    private void validateSlotTimes(AvailabilityRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot set availability for a past date");
        }
    }

    private DoctorProfile getDoctorByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Doctor profile not found for authenticated user"));
    }

    public AvailabilityDto mapToDto(DoctorAvailability a) {
        return new AvailabilityDto(
                a.getId(),
                a.getDoctor().getId(),
                a.getDoctor().getUser().getFullName(),
                a.getAppointmentDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.isAvailable()
        );
    }
}
