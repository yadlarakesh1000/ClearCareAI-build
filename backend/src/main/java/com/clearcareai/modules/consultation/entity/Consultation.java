package com.clearcareai.modules.consultation.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.clearcareai.modules.Appointment.entity.Appointment;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.patient.entity.Patient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@NoArgsConstructor 
@AllArgsConstructor 
@Getter 
@Setter 
@Builder 
@Table (name="consultations")
public class Consultation {
    
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @OneToOne(fetch = FetchType.LAZY)
   @JoinColumn (name = "appointment_id",nullable = false,unique = true)
  private Appointment appointment;

  @ManyToOne 
  @JoinColumn (name="doctor_id",nullable = false)
  private Doctor doctor;
  @ManyToOne 
  @JoinColumn (name="patient_id",nullable = false)
  private Patient patient;
  
   @Column(columnDefinition = "TEXT")
  private String diagnosis;
   @Column(columnDefinition = "TEXT")
  private String prescription;
   @Column(columnDefinition = "TEXT")
  private String notes;
  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  @Builder.Default
  private Status status=Status.IN_PROGRESS;

  @CreationTimestamp
   @Column(name = "created_at", updatable = false)
   private LocalDateTime createdAt; 
   @UpdateTimestamp 
   @Column (name = "updated_at")
   private  LocalDateTime updatedAt;
   public enum Status{
    IN_PROGRESS,COMPLETED
   }
}