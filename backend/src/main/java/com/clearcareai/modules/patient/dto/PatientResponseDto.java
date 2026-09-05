package com.clearcareai.modules.patient.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PatientResponseDto {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private LocalDate dateofBirth;
        private String gender;
        private String bloodGroup;
        private String address;
        private String medicalHisotry;
}
