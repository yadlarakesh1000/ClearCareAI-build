package com.clearcareai.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// The two endpoints FastAPI calls must reject anyone who does not know the secret.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.internal-secret.enabled=true",
        "app.internal-secret.value=test-secret-value"
})
class InternalSecretFilterTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void processReview_withoutSecret_is401() throws Exception {
        mockMvc.perform(put("/api/reviews/1/process")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void processReview_withWrongSecret_is401() throws Exception {
        mockMvc.perform(put("/api/reviews/1/process")
                        .header(InternalSecretFilter.INTERNAL_SECRET_HEADER, "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void byCallId_withoutSecret_is401() throws Exception {
        mockMvc.perform(get("/api/reviews/by-call-id/999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void byCallId_withCorrectSecret_reachesTheService() throws Exception {
        // secret accepted; review 999 does not exist, so the service answers 404
        mockMvc.perform(get("/api/reviews/by-call-id/999")
                        .header(InternalSecretFilter.INTERNAL_SECRET_HEADER, "test-secret-value"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicEndpoint_isNotAffected() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk());
    }
}
