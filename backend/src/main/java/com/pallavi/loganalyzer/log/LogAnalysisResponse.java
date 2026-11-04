package com.pallavi.loganalyzer.log;

import java.util.List;

public record LogAnalysisResponse(
        Long logId,
        String filename,
        int totalLines,
        int parsedEntries,
        int errorCount,
        int warningCount,
        int infoCount,
        int debugCount,
        int traceCount,
        int otherCount,
        List<LogEntryResponse> entries
) {
}
