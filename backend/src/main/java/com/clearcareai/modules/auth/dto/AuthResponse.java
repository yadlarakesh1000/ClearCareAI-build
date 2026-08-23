package com.clearcareai.modules.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    
  private Long userId;
  private String email;
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private String role;
}
