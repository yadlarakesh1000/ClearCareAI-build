package com.clearcareai.modules.consultation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clearcareai.modules.consultation.entity.Consultation;

public interface ConsultationRepository extends JpaRepository<Consultation,Long> {
  Optional<Consultation> findByAppointmentId(Long appointmentId);
  boolean existsByAppointmemtId(Long appointmentId);
 List<Consultation> findByStatusAndUpdatedAtBefore(Consultation.Status Status, LocalDateTime cutoff);
  
}
