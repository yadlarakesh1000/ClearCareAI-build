package com.clearcareai.modules.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="refresh_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name="token",nullable=false,unique=true)
  private String token;
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name = "user_id",nullable=false)
  private User user;
  @Column(name="is_revoked")
  @Builder.Default
  private Boolean isRevoked=false;
  @Column(name="expiry_date",nullable  =false)
  private LocalDateTime expiryDate;
  @Column(name="created_at",updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  
}
