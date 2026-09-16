package com.clearcareai.modules.Appointment.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data 
public class AppointmentRequestDto {

  @NotNull(message = "Doctor Id is required")
  private Long doctorId;
  @NotNull (message = "Appointment date required")
  private LocalDate appointmentDate;
  @NotNull (message = "Slot ID is Required")
  private  Long slotId;
  
  
}
