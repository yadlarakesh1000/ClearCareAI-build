package com.clearcareai.modules.slot.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SlotResponseDto {
       
  private Long id;
  private Long doctorId;
  private String dayOfWeek;
  private LocalTime starTime;
  private LocalTime endTime;
  private Boolean isActive;
}
