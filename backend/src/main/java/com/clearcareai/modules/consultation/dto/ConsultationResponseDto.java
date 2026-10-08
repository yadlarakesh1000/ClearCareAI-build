package com.clearcareai.modules.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class ConsultationResponseDto {
      private Long id;
      private Long appointmentId;
      private Long doctorId;
      private String doctorName;
      private Long patientId;
      private String patientName;
      private String diagnosis;
      private String prescription;
      private String notes;
      private String status;
}
