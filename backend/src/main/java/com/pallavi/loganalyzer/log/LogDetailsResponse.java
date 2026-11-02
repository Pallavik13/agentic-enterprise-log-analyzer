package com.pallavi.loganalyzer.log;

import java.time.Instant;

public record LogDetailsResponse(
        Long id,
        String filename,
        String status,
        Instant uploadedAt,
        String content
) {
}
