
package com.pallavi.loganalyzer.log;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogParserService {

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{1,9})?(?:Z|[+-]\\d{2}:?\\d{2})?)"
    );

    private static final Pattern LEVEL_PATTERN = Pattern.compile(
            "^\\s*(ERROR|WARN(?:ING)?|INFO|DEBUG|TRACE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STACK_TRACE_PATTERN = Pattern.compile(
            "^\\s*(?:at\\s+.+|Caused by:.*|\\.\\.\\. \\d+ more)$"
    );

    public LogAnalysisResponse analyze(
            Long logId,
            String filename,
            String content) {

        List<LogEntryResponse> entries = new ArrayList<>();

        int errors = 0;
        int warnings = 0;
        int infos = 0;
        int debugs = 0;
        int traces = 0;
        int others = 0;

        String[] lines = (content == null || content.isEmpty())
                ? new String[0]
                : content.split("\\R");

        for (int i = 0; i < lines.length; i++) {

            String line = lines[i];

            if (line.isBlank()) {
                continue;
            }

            String timestamp = null;
            String remaining = line;

            Matcher timestampMatcher =
                    TIMESTAMP_PATTERN.matcher(line);

            if (timestampMatcher.find()) {
                timestamp = timestampMatcher.group(1);
                remaining = line.substring(
                        timestampMatcher.end()
                ).trim();
            }

            Matcher levelMatcher =
                    LEVEL_PATTERN.matcher(remaining);

            if (!levelMatcher.find()
                    && STACK_TRACE_PATTERN.matcher(line).matches()
                    && !entries.isEmpty()) {

                LogEntryResponse previous =
                        entries.get(entries.size() - 1);

                entries.set(
                        entries.size() - 1,
                        new LogEntryResponse(
                                previous.lineNumber(),
                                previous.timestamp(),
                                previous.level(),
                                previous.message() + "\n" + line,
                                previous.rawLine() + "\n" + line
                        )
                );

                continue;
            }

            String level = "OTHER";
            String message = remaining;

            if (levelMatcher.find(0)) {

                level = levelMatcher.group(1)
                        .toUpperCase(Locale.ROOT);

                if (level.equals("WARNING")) {
                    level = "WARN";
                }

                message = remaining.substring(
                        levelMatcher.end()
                ).replaceFirst("^\\s*[:\\-]\\s*", "").trim();
            } else {
                message = line;
            }

            switch (level) {
                case "ERROR" -> errors++;
                case "WARN" -> warnings++;
                case "INFO" -> infos++;
                case "DEBUG" -> debugs++;
                case "TRACE" -> traces++;
                default -> others++;
            }

            entries.add(new LogEntryResponse(
                    i + 1,
                    timestamp,
                    level,
                    message,
                    line
            ));
        }

        return new LogAnalysisResponse(
                logId,
                filename,
                lines.length,
                entries.size(),
                errors,
                warnings,
                infos,
                debugs,
                traces,
                others,
                entries
        );
    }
}
