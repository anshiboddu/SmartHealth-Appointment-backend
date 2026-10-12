package com.healthappointment.repository;

import com.healthappointment.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorIdOrderByAppointmentDateAscStartTimeAsc(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(Long doctorId, LocalDate appointmentDate);

    List<DoctorAvailability> findByDoctorIdAndAppointmentDateAndAvailableTrueOrderByStartTimeAsc(
            Long doctorId, LocalDate appointmentDate);

    List<DoctorAvailability> findByDoctorIdAndAppointmentDateGreaterThanEqualAndAvailableTrueOrderByAppointmentDateAscStartTimeAsc(
            Long doctorId, LocalDate fromDate);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM DoctorAvailability a " +
           "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsOverlappingSlot(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
