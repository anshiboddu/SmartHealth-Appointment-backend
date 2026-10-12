package com.healthappointment;

import com.healthappointment.dto.AuthResponse;
import com.healthappointment.dto.RegisterRequest;
import com.healthappointment.entity.User;
import com.healthappointment.enums.Role;
import com.healthappointment.exception.ConflictException;
import com.healthappointment.repository.DoctorProfileRepository;
import com.healthappointment.repository.UserRepository;
import com.healthappointment.security.JwtTokenProvider;
import com.healthappointment.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                doctorProfileRepository,
                passwordEncoder,
                authenticationManager,
                tokenProvider
        );
    }

    @Test
    void testRegisterPatientSuccess() {
        RegisterRequest request = new RegisterRequest("Alice Wonderland", "alice@example.com", "9876543210", "Password@123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        User savedUser = new User("Alice Wonderland", "alice@example.com", "9876543210", "encodedPassword", Role.PATIENT);
        savedUser.setId(10L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mockJwtToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(Role.PATIENT, response.getRole());
        assertEquals(10L, response.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailThrowsConflict() {
        RegisterRequest request = new RegisterRequest("Alice Wonderland", "alice@example.com", "9876543210", "Password@123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
