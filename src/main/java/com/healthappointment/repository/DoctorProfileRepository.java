package com.healthappointment.repository;

import com.healthappointment.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    List<DoctorProfile> findByDepartmentIdAndActiveTrue(Long departmentId);

    @Query("SELECT d FROM DoctorProfile d WHERE LOWER(d.department.name) = LOWER(:deptName) AND d.active = true AND d.user.active = true")
    List<DoctorProfile> findByDepartmentNameIgnoreCaseAndActiveTrue(@Param("deptName") String deptName);

    @Query("SELECT d FROM DoctorProfile d WHERE d.active = true AND d.user.active = true")
    List<DoctorProfile> findAllActiveDoctors();

    long countByActiveTrue();

    long countByDepartmentId(Long departmentId);
}
