package com.clearcareai.modules.Appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
@Data 
public class AppointmentResponseDto {

      private  Long id;
      private  Long patientId;
      private String patientName;
      private  Long doctorId;
      private  String doctorName;
      private Long slotId;
      private String specialization;
      private LocalDate appointmentDate;
      private LocalTime startTime;
      private  LocalTime endTime;
      private String status;
      


}