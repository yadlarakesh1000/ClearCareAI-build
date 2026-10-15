package com.clearcareai.modules.voicereview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class VoiceReviewTriggerRequest {
       @NotNull (message = "consultation Id is required")
      private Long consultationId;
}
