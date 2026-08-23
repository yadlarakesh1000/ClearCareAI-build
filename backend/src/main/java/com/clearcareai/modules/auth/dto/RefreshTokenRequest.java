package com.clearcareai.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenRequest {
  
  @NotBlank(message = "refresh Token should not be blank")
  private String refreshToken;
}
