package com.clearcareai.modules.voicereview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Component 
@ConfigurationProperties (prefix = "app.voicereview")
@Validated 
@Getter 
@Setter 
public class VoiceReviewProperties {
  
  @NotNull 
  @Positive 
  private Long initialDelayDays;
  @NotNull 
  @Positive 
  private Long retryGapHours;
  @NotNull 
  @Positive 
  private Integer maxAttempts;
  @NotNull 
  @Positive 
  private Long checkIntervalMs;
}
