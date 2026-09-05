package com.clearcareai.modules.patient.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequestDto {
  @NotNull(message="Date of birth is required")
  @PastOrPresent(message="Date of birth cant be in the future")
  private LocalDateTime dateofBirth;
  @NotBlank(message="Gender required")
  @Pattern(regexp = "^(MALE|FEMALE|OTHER)$",message="Gender must be one of MALE,FEMALE,OTHER")
  private String gender;

  private String bloodGroup;
  private String medicalHistory;
  private String address;



}
