package com.healthappointment.repository;

import com.healthappointment.entity.Appointment;
import com.healthappointment.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

    List<Appointment> findAllByOrderByAppointmentDateDescStartTimeDesc();

    List<Appointment> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date " +
           "AND a.status IN :activeStatuses " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsDoctorOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses,
            @Param("excludeId") Long excludeId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.patient.id = :patientId AND a.appointmentDate = :date " +
           "AND a.status IN :activeStatuses " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsPatientOverlappingAppointment(
            @Param("patientId") Long patientId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses,
            @Param("excludeId") Long excludeId);

    long countByStatus(AppointmentStatus status);

    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}
