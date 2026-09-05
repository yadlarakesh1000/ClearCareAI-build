package com.clearcareai.modules.doctor.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.clearcareai.modules.auth.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Doctor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id",nullable = false,unique = true  )
  private User user;
  @Column(nullable = false,length = 100)
  private String specialization;
  @Column(nullable=false,length = 200)
  private String qualification;
  @Column(nullable = false,name = "experience_years")
  private Integer experience;
  @Column(columnDefinition = "TEXT")
  private String bio;
    @Column(nullable = false,name="consultation_fee",precision = 10,scale = 2)
  private BigDecimal consultationfee;
  @Column(name = "is_available",nullable = false)
  @Builder.Default()
  private Boolean isAvailable=true;
  @CreationTimestamp
  @Column(name = "created_at",updatable = false)
  private LocalDateTime createdAt;
  @Column(name="updated_at")
  private LocalDateTime updatedAt;

  
}
