package com.clearcareai.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Proves the controller paths now match the role rules in SecurityConfig.
// Before the fix the controllers used singular paths (api/consultation), so the
// plural role rules never applied and the plural URLs returned 404.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RouteSecurityTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void patient_cannotUpdateConsultation() throws Exception {
        mockMvc.perform(put("/api/consultations/1")
                        .with(user("patient@test.com").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void patient_cannotTriggerVoiceReview() throws Exception {
        mockMvc.perform(post("/api/voice-reviews/trigger")
                        .with(user("patient@test.com").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"consultationId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctor_cannotBookAppointment() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .with(user("doctor@test.com").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctor_reachesConsultationUpdateEndpoint() throws Exception {
        // the endpoint exists and the role is allowed; the mock user has no row
        // in the database, so the service throws ConsultationException("User not
        // found"), which is a 400 - not a 403, 404 (no route), 405 or 500
        mockMvc.perform(put("/api/consultations/1")
                        .with(user("doctor@test.com").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"diagnosis\":\"flu\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void doctorAnalytics_pathVariableBinds() throws Exception {
        // before the fix this was a 500 (no {doctorId} in the path)
        mockMvc.perform(get("/api/analytics/doctor/999")
                        .with(user("doctor@test.com").roles("DOCTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void textReview_bodyIsValidated() throws Exception {
        // before the fix @RequestBody sat on the wrong parameter, so the JSON
        // body never reached ReviewRequestDto. An empty body must now be a 400.
        mockMvc.perform(post("/api/reviews")
                        .with(user("patient@test.com").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void internalEndpoint_isOpenWhenSecretCheckIsOff() throws Exception {
        // default config: enabled=false, so the call reaches the service (review 999 not found)
        mockMvc.perform(get("/api/reviews/by-call-id/999"))
                .andExpect(status().isNotFound());
    }
}
