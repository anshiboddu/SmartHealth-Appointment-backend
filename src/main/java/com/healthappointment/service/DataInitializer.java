package com.healthappointment.service;

import com.healthappointment.entity.*;
import com.healthappointment.enums.Role;
import com.healthappointment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@smarthealth.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    @Value("${app.admin.phone:1234567890}")
    private String adminPhone;

    public DataInitializer(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           DoctorProfileRepository doctorProfileRepository,
                           DoctorAvailabilityRepository availabilityRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.availabilityRepository = availabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Checking database initialization...");

        // 1. Seed Admin
        seedAdminUser();

        // 2. Seed Departments
        seedDepartments();

        // 3. Seed Sample Doctors
        seedSampleDoctors();

        // 4. Seed Demo Patient
        seedDemoPatient();

        logger.info("Database initialization completed successfully.");
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail(adminEmail.toLowerCase().trim())) {
            User admin = new User(
                    adminName,
                    adminEmail.toLowerCase().trim(),
                    adminPhone,
                    passwordEncoder.encode(adminPassword),
                    Role.ADMIN
            );
            userRepository.save(admin);
            logger.info("Seeded initial Admin user: {}", adminEmail);
        }
    }

    private void seedDepartments() {
        createDepartmentIfNotExists(
                "General Medicine",
                "Comprehensive primary health care, diagnosis of common acute illnesses, fevers, headaches, internal disorders, and preventative medicine."
        );
        createDepartmentIfNotExists(
                "Dermatology",
                "Specialized diagnosis and treatment of skin conditions, acne, rashes, eczema, hair, and nail disorders."
        );
        createDepartmentIfNotExists(
                "Dentistry",
                "Oral healthcare, toothache treatment, cavities, teeth cleaning, gum therapies, and dental consultations."
        );
        createDepartmentIfNotExists(
                "Ophthalmology",
                "Comprehensive ocular diagnostics, vision assessment, eye irritation, infections, and refractive care."
        );
        createDepartmentIfNotExists(
                "Orthopedics",
                "Diagnosis and treatment of musculoskeletal conditions, joint pain, spine/back pain, sports injuries, and arthritis."
        );
    }

    private Department createDepartmentIfNotExists(String name, String description) {
        Optional<Department> existing = departmentRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        Department dept = new Department(name, description);
        return departmentRepository.save(dept);
    }

    private void seedSampleDoctors() {
        Department genMed = departmentRepository.findByNameIgnoreCase("General Medicine").orElse(null);
        Department dermatology = departmentRepository.findByNameIgnoreCase("Dermatology").orElse(null);
        Department dentistry = departmentRepository.findByNameIgnoreCase("Dentistry").orElse(null);
        Department ophthalmology = departmentRepository.findByNameIgnoreCase("Ophthalmology").orElse(null);
        Department orthopedics = departmentRepository.findByNameIgnoreCase("Orthopedics").orElse(null);

        if (genMed != null) {
            seedDoctor(
                    "Dr. Sarah Jenkins", "dr.sarah@smarthealth.com", "9123456781", "Doctor@123",
                    genMed, "Internal Medicine & General Physician", "MBBS, MD (General Medicine)",
                    10, 50.0,
                    "Experienced general physician specializing in primary healthcare, fevers, lifestyle management, and preventative screening."
            );
        }

        if (dermatology != null) {
            seedDoctor(
                    "Dr. David Miller", "dr.david@smarthealth.com", "9123456782", "Doctor@123",
                    dermatology, "Clinical Dermatology & Cosmetology", "MBBS, MD (Dermatology)",
                    8, 60.0,
                    "Specialist in skin conditions, acne treatment, allergic dermatitis, and modern therapeutic dermatology."
            );
        }

        if (dentistry != null) {
            seedDoctor(
                    "Dr. Emily Clark", "dr.emily@smarthealth.com", "9123456783", "Doctor@123",
                    dentistry, "Conservative Dentistry & Endodontics", "BDS, MDS (Endodontics)",
                    7, 45.0,
                    "Dedicated dental surgeon providing gentle oral care, pain relief, root canals, and aesthetic restorations."
            );
        }

        if (ophthalmology != null) {
            seedDoctor(
                    "Dr. Robert Taylor", "dr.robert@smarthealth.com", "9123456784", "Doctor@123",
                    ophthalmology, "Comprehensive Ophthalmology", "MBBS, MS (Ophthalmology)",
                    12, 55.0,
                    "Expert ophthalmologist treating eye irritation, dry eye disease, visual disorders, and general ocular wellness."
            );
        }

        if (orthopedics != null) {
            seedDoctor(
                    "Dr. Michael Brown", "dr.michael@smarthealth.com", "9123456785", "Doctor@123",
                    orthopedics, "Joint Reconstruction & Spine Care", "MBBS, MS (Orthopedics), DNB",
                    14, 70.0,
                    "Senior orthopedic surgeon focusing on back pain, chronic joint inflammation, knee arthroscopy, and rehabilitation."
            );
        }
    }

    private void seedDoctor(String fullName, String email, String phone, String rawPassword,
                            Department department, String specialization, String qualification,
                            int experienceYears, double consultationFee, String bio) {
        String cleanEmail = email.toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            return;
        }

        User user = new User(
                fullName,
                cleanEmail,
                phone,
                passwordEncoder.encode(rawPassword),
                Role.DOCTOR
        );
        User savedUser = userRepository.save(user);

        DoctorProfile profile = new DoctorProfile(
                savedUser,
                department,
                specialization,
                qualification,
                experienceYears,
                consultationFee,
                bio
        );
        DoctorProfile savedProfile = doctorProfileRepository.save(profile);

        // Seed availability slots for today and next 5 days
        seedAvailabilitySlots(savedProfile);
        logger.info("Seeded Doctor: {} ({})", fullName, cleanEmail);
    }

    private void seedAvailabilitySlots(DoctorProfile doctor) {
        LocalDate today = LocalDate.now();
        LocalTime[][] timeWindows = new LocalTime[][]{
                {LocalTime.of(9, 0), LocalTime.of(10, 0)},
                {LocalTime.of(10, 30), LocalTime.of(11, 30)},
                {LocalTime.of(14, 0), LocalTime.of(15, 0)},
                {LocalTime.of(15, 30), LocalTime.of(16, 30)}
        };

        for (int i = 0; i <= 5; i++) {
            LocalDate date = today.plusDays(i);
            for (LocalTime[] slot : timeWindows) {
                DoctorAvailability availability = new DoctorAvailability(
                        doctor,
                        date,
                        slot[0],
                        slot[1]
                );
                availabilityRepository.save(availability);
            }
        }
    }

    private void seedDemoPatient() {
        String patientEmail = "patient@smarthealth.com";
        if (!userRepository.existsByEmail(patientEmail)) {
            User patient = new User(
                    "John Doe",
                    patientEmail,
                    "9876543210",
                    passwordEncoder.encode("Patient@123"),
                    Role.PATIENT
            );
            userRepository.save(patient);
            logger.info("Seeded demo Patient: {}", patientEmail);
        }
    }
}
