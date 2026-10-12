package com.healthappointment.service;

import com.healthappointment.dto.AuthResponse;
import com.healthappointment.dto.LoginRequest;
import com.healthappointment.dto.RegisterRequest;
import com.healthappointment.dto.UserProfileUpdateRequest;
import com.healthappointment.dto.UserSummaryDto;
import com.healthappointment.entity.DoctorProfile;
import com.healthappointment.entity.User;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.BadRequestException;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.exception.ResourceNotFoundException;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.repository.UserRepository;
import com.healthappointment.security.JwtTokenProvider;
import com.healthappointment.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       DoctorProfileRepository doctorProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        // Public registration always assigns PATIENT role (never DOCTOR or ADMIN)
        User user = new User(
                request.getFullName().trim(),
                request.getEmail().toLowerCase().trim(),
                request.getPhone().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.PATIENT
        );

        User savedUser = userRepository.save(user);

        // Authenticate the registered user immediately
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getPhone(),
                null
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new BadRequestException("Your account is deactivated. Please contact administrator.");
        }

        Long doctorProfileId = null;
        if (user.getRole() == Role.DOCTOR) {
            Optional<DoctorProfile> profile = doctorProfileRepository.findByUserId(user.getId());
            if (profile.isPresent()) {
                doctorProfileId = profile.get().getId();
            }
        }

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                doctorProfileId
        );
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));

        Long doctorProfileId = null;
        if (user.getRole() == Role.DOCTOR) {
            doctorProfileId = doctorProfileRepository.findByUserId(user.getId())
                    .map(DoctorProfile::getId)
                    .orElse(null);
        }

        return new UserSummaryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                doctorProfileId
        );
    }

    @Transactional
    public UserSummaryDto updateProfile(UserPrincipal userPrincipal, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        // Role cannot be changed via profile update!

        User updatedUser = userRepository.save(user);

        Long doctorProfileId = null;
        if (updatedUser.getRole() == Role.DOCTOR) {
            doctorProfileId = doctorProfileRepository.findByUserId(updatedUser.getId())
                    .map(DoctorProfile::getId)
                    .orElse(null);
        }

        return new UserSummaryDto(
                updatedUser.getId(),
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getRole(),
                updatedUser.isActive(),
                updatedUser.getCreatedAt(),
                doctorProfileId
        );
    }
}
