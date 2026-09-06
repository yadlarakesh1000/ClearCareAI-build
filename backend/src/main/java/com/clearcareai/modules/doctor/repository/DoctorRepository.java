package com.clearcareai.modules.doctor.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clearcareai.modules.doctor.entity.Doctor;


public interface DoctorRepository extends JpaRepository<Doctor,Long> {

  Optional<Doctor> findByUserId(Long userId);
  boolean existsByUserId(Long userId);
  Page<Doctor> findBySpecializationContainingIgnoreCase(String specialization,Pageable pageable);
  @Query("SELECT d FROM Doctor d JOIN d.user u "
            + "WHERE LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Doctor> searchDoctors(@Param("search") String search,Pageable pageable);
  
}
