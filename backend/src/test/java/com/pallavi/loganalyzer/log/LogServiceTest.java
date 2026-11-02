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
import java.util.List;
import java.util.Optional;

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

                        var timeField =
                                LogFile.class.getDeclaredField("uploadedAt");
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

        verify(logFileRepository).save(any(LogFile.class));
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

        verify(logFileRepository, never()).save(any(LogFile.class));
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

        verify(logFileRepository, never()).save(any(LogFile.class));
    }

    @Test
    void shouldReturnAllUploadedLogs() {

        LogFile first = mock(LogFile.class);
        LogFile second = mock(LogFile.class);

        when(first.getId()).thenReturn(1L);
        when(first.getOriginalFilename()).thenReturn("app.log");
        when(first.getStatus()).thenReturn("RECEIVED");
        when(first.getUploadedAt()).thenReturn(Instant.now());

        when(second.getId()).thenReturn(2L);
        when(second.getOriginalFilename()).thenReturn("server.txt");
        when(second.getStatus()).thenReturn("RECEIVED");
        when(second.getUploadedAt()).thenReturn(Instant.now());

        when(logFileRepository.findAll())
                .thenReturn(List.of(first, second));

        List<LogUploadResponse> result = logService.getAllLogs();

        assertEquals(2, result.size());
        assertEquals("app.log", result.get(0).filename());
        assertEquals("server.txt", result.get(1).filename());

        verify(logFileRepository).findAll();
    }

    @Test
    void shouldReturnLogDetailsById() {

        LogFile log = mock(LogFile.class);

        when(log.getId()).thenReturn(1L);
        when(log.getOriginalFilename()).thenReturn("application.log");
        when(log.getStatus()).thenReturn("RECEIVED");
        when(log.getUploadedAt()).thenReturn(Instant.now());
        when(log.getContent()).thenReturn("ERROR: Database unavailable");

        when(logFileRepository.findById(1L))
                .thenReturn(Optional.of(log));

        LogDetailsResponse result = logService.getLogById(1L);

        assertEquals(1L, result.id());
        assertEquals("application.log", result.filename());
        assertEquals("ERROR: Database unavailable", result.content());

        verify(logFileRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenLogDoesNotExist() {

        when(logFileRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> logService.getLogById(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(logFileRepository).findById(999L);
    }
}
