package com.healthappointment.repository;

import com.healthappointment.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Department> findByActiveTrueOrderByNameAsc();

    List<Department> findAllByOrderByNameAsc();

    long countByActiveTrue();
}
