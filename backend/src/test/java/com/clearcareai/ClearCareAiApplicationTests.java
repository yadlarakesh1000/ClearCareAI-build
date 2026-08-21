package com.clearcareai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")     // <-- ADDED: run against H2, not MySQL
class ClearCareAiApplicationTests {

    // proves the whole application context can start without errors
    @Test
    void contextLoads() {
    }

}
