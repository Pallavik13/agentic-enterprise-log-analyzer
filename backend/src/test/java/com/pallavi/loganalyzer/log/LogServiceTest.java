package com.pallavi.loganalyzer.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogFileRepository logFileRepository;

    @InjectMocks
    private LogService logService;

    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        validFile = new MockMultipartFile(
                "file",
                "application.log",
                "text/plain",
                "ERROR: Database connection failed".getBytes()
        );
    }

    @Test
    void shouldUploadValidLogFile() {

        when(logFileRepository.save(any(LogFile.class)))
                .thenAnswer(invocation -> {
                    LogFile log = invocation.getArgument(0);

                    try {
                        var idField = LogFile.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(log, 1L);

                        var timeField = LogFile.class.getDeclaredField("uploadedAt");
                        timeField.setAccessible(true);
                        timeField.set(log, Instant.now());

                    } catch (ReflectiveOperationException exception) {
                        throw new RuntimeException(exception);
                    }

                    return log;
                });

        LogUploadResponse response = logService.uploadLog(validFile);

        assertEquals(1L, response.id());
        assertEquals("application.log", response.filename());
        assertEquals("RECEIVED", response.status());

        verify(logFileRepository, times(1))
                .save(any(LogFile.class));
    }

    @Test
    void shouldRejectUnsupportedFileType() {

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "sample content".getBytes()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> logService.uploadLog(invalidFile)
        );

        verify(logFileRepository, never())
                .save(any(LogFile.class));
    }

    @Test
    void shouldRejectEmptyFile() {

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.log",
                "text/plain",
                new byte[0]
        );

        assertThrows(
                ResponseStatusException.class,
                () -> logService.uploadLog(emptyFile)
        );

        verify(logFileRepository, never())
                .save(any(LogFile.class));
    }
}
