package com.clearcareai.modules.doctor.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DoctorResponseDto {
       private Long id;

    // flattened from Doctor.user by the mapper
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // straight from the entity
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private Boolean isAvailable;
    //later 
    private Double averageRating;
    private Long totalReviews;
}
