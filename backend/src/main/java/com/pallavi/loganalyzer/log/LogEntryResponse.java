package com.pallavi.loganalyzer.log;

public record LogEntryResponse(
        int lineNumber,
        String timestamp,
        String level,
        String message,
        String rawLine
) {
}
