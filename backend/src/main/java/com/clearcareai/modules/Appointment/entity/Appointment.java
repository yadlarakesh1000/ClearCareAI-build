package com.clearcareai.modules.Appointment.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.patient.entity.Patient;
import com.clearcareai.modules.slot.entity.Slot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name="appointments",
uniqueConstraints= @UniqueConstraint(
     name="unique_active_appointment",
     columnNames={"doctor_id","slot_id","appointment_date","active_booking"}
))
@AllArgsConstructor 
@NoArgsConstructor 
@Data 
@Builder 
public class Appointment {
  
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn (name = "patient_id",nullable = false)
  private  Patient patient;
   @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn (name = "doctor_id",nullable = false)
  private  Doctor doctor;
   @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn (name = "slot_id",nullable = false)
  private Slot slot;
   @Column (name = "appointment_date",nullable = false)
  private LocalDate appointmentDate;
  @Enumerated (EnumType.STRING)
  @Column (nullable = false)
  @Builder.Default
  private Status status=Status.BOOKED;
  @Column (name = "active_booking")
  private Boolean activeBooking;
  @Column (columnDefinition = "TEXT" )
  private String notes;
  @CreationTimestamp 
  @Column(name="created_at",updatable = false )
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name="updated_at")
  private LocalDateTime updatedAt;
   public enum Status {
      BOOKED,CANCELLED,COMPLETED
  }
  
}
