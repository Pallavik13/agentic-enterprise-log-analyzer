package com.pallavi.loganalyzer.log;

import java.time.Instant;

public record LogUploadResponse(
        Long id,
        String filename,
        String status,
        Instant uploadedAt
) {
}
