package com.clearcareai.modules.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name="first_name",nullable=false)
  private String firstName;
  @Column(name="last_name",nullable=false)
  private String lastName;
  @Column(nullable = false,unique = true,length=100)
  private String email;
  @Column(nullable = false)
  private String password;
  @Column(nullable=false,length=15)
  private String phone;
  @CreationTimestamp
  @Column(name="created_at",updatable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name="updated_at")
  private LocalDateTime updatedAt;
  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private User.Role role;
   @Column(name="is_active")
   @Builder.Default()
   private Boolean isActive=true;


   public enum Role{
    ROLE_PATIENT,ROLE_DOCTOR,ROLE_ADMIN


   }
  
}
