package com.clearcareai.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

// @Getter/@Setter, NOT @Data - @Data would generate a toString() containing the secret
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.internal-secret")
public class InternalSecretProperties {

    // the shared secret itself. No @NotBlank: local dev runs without one.
    private String value;

    // false = the filter lets everything through (local dev and tests)
    private Boolean enabled = false;
}
