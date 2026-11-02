package com.pallavi.loganalyzer.log;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogService logService;

    @Test
    void shouldReturnAllLogs() throws Exception {

        when(logService.getAllLogs()).thenReturn(List.of(
                new LogUploadResponse(
                        1L,
                        "application.log",
                        "RECEIVED",
                        Instant.parse("2026-11-02T10:00:00Z")
                )
        ));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].filename")
                        .value("application.log"));
    }

    @Test
    void shouldReturnLogById() throws Exception {

        when(logService.getLogById(1L)).thenReturn(
                new LogDetailsResponse(
                        1L,
                        "application.log",
                        "RECEIVED",
                        Instant.parse("2026-11-02T10:00:00Z"),
                        "ERROR: Database unavailable"
                )
        );

        mockMvc.perform(get("/api/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content")
                        .value("ERROR: Database unavailable"));
    }
}
