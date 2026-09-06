package com.clearcareai.modules.slot.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotRequestDto {

      @NotBlank(message = "Day of week is required")
      @Pattern(regexp = "^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)$",message = "Day of week must be a valid day (MONDAY-SUNDAY)" )
      private String dayOfWeek;
      @NotNull(message = "Start time is required")
      private LocalTime starTime;
      @NotNull(message = "End time is required")
      private LocalTime endTime;
      

}
