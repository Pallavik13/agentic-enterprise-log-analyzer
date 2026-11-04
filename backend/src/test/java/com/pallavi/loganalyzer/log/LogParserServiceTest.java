package com.pallavi.loganalyzer.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogParserServiceTest {

    private final LogParserService parser = new LogParserService();

    @Test
    void shouldCountDifferentLogLevels() {

        String content = """
                2026-11-04 10:00:01 INFO Application started
                2026-11-04 10:00:05 WARN High memory usage
                2026-11-04 10:00:10 ERROR Database connection failed
                2026-11-04 10:00:12 ERROR Retry attempt failed
                2026-11-04 10:00:15 DEBUG Retrying connection
                2026-11-04 10:00:20 TRACE Request details
                """;

        LogAnalysisResponse result =
                parser.analyze(1L, "application.log", content);

        assertEquals(6, result.totalLines());
        assertEquals(6, result.parsedEntries());
        assertEquals(2, result.errorCount());
        assertEquals(1, result.warningCount());
        assertEquals(1, result.infoCount());
        assertEquals(1, result.debugCount());
        assertEquals(1, result.traceCount());
        assertEquals(0, result.otherCount());
    }

    @Test
    void shouldNormalizeWarningAndMixedCaseLevels() {

        String content = """
                warning: Disk space is low
                error: Service unavailable
                Info: Request completed
                """;

        LogAnalysisResponse result =
                parser.analyze(2L, "system.log", content);

        assertEquals(1, result.warningCount());
        assertEquals(1, result.errorCount());
        assertEquals(1, result.infoCount());
    }

    @Test
    void shouldPreserveLineNumbersAndRawContent() {

        String content = """
                INFO Application started

                ERROR Database unavailable
                """;

        LogAnalysisResponse result =
                parser.analyze(3L, "application.log", content);

        assertEquals(2, result.parsedEntries());
        assertEquals(1, result.entries().get(0).lineNumber());
        assertEquals(3, result.entries().get(1).lineNumber());
        assertEquals(
                "ERROR Database unavailable",
                result.entries().get(1).rawLine()
        );
    }

    @Test
    void shouldIdentifyUnclassifiedEntries() {

        String content = """
                Custom application event
                INFO Application started
                """;

        LogAnalysisResponse result =
                parser.analyze(4L, "custom.log", content);

        assertEquals(1, result.otherCount());
        assertEquals(1, result.infoCount());
    }

    @Test
    void shouldReturnEmptyAnalysisForEmptyContent() {

        LogAnalysisResponse result =
                parser.analyze(5L, "empty.log", "");

        assertEquals(0, result.totalLines());
        assertEquals(0, result.parsedEntries());
        assertEquals(0, result.errorCount());
        assertTrue(result.entries().isEmpty());
    }

    @Test
    void shouldExtractTimestamp() {

        String content =
                "2026-11-04 10:30:45 ERROR Database connection failed";

        LogAnalysisResponse result =
                parser.analyze(6L, "database.log", content);

        assertEquals(
                "2026-11-04 10:30:45",
                result.entries().get(0).timestamp()
        );
        assertEquals("ERROR", result.entries().get(0).level());
    }
}
