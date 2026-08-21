package com.clearcareai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // used later to call OmniDim (B6) and the FastAPI service (A1)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
