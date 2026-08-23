package com.clearcareai.modules.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {
  @Email(message="email required")
  @NotBlank(message = "Email is Required")
  private String email;
  @NotBlank(message="Password should not blank")
  private String password;
}
