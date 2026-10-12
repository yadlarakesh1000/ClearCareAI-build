package com.clearcareai.modules.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data 
public class ReviewFlagRequest {
  
  @NotBlank (message = "Reason is required")
  private String flagReason;
  
}
