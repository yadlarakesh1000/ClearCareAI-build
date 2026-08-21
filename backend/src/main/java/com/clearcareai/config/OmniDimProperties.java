package com.clearcareai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

// @Getter/@Setter, NOT @Data - @Data would generate a toString() containing the API key
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.omnidim")
public class OmniDimProperties {

    @NotBlank
    private String apiKey;

    // Integer, not String - the real OmniDim API requires a numeric agent_id
    @NotNull
    private Integer agentId;

    @NotBlank
    private String baseUrl;
}
