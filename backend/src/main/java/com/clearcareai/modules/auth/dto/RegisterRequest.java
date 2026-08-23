package com.clearcareai.modules.auth.dto;


import com.clearcareai.modules.auth.validator.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
     @NotBlank(message="Email is Required")
     @Email(message = "Email must be valid")
      private String email;
      @NotBlank(message="first name required")
      private String firstName;
      @NotBlank(message="last name required")
      private String lastName;
      @NotBlank(message="Password required")
      @ValidPassword
      private String password;
      @NotBlank(message = "phone is required")
      @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
      private String phone;
      @NotBlank(message="role is required")
      @Pattern(regexp = "^(ROLE_PATIENT|ROLE_DOCTOR|ROLE_ADMIN)$",
             message = "Role must be one of ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN")
      private String role;
  
}