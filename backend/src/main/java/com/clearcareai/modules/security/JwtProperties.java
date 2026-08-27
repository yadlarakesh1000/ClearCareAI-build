package com.clearcareai.modules.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app.jwt")
@Component
@Validated
@Getter@Setter
public class JwtProperties {
   @NotBlank
   @Size(min=32)
  private String secret;
  @NotNull
  @Positive
  private Long expiration;
  
  @NotNull
  @Positive
  private Long refreshExpiration;
  
}
