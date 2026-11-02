package com.pallavi.loganalyzer.log;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LogService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final LogFileRepository logFileRepository;

    public LogService(LogFileRepository logFileRepository) {
        this.logFileRepository = logFileRepository;
    }

    public LogUploadResponse uploadLog(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded file cannot be empty"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size cannot exceed 5 MB"
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename is required"
            );
        }

        String safeFilename = filename.replace("\\", "/");
        safeFilename = safeFilename.substring(
                safeFilename.lastIndexOf('/') + 1
        );

        String lowerCaseFilename = safeFilename.toLowerCase();

        if (!lowerCaseFilename.endsWith(".log")
                && !lowerCaseFilename.endsWith(".txt")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only .log and .txt files are supported"
            );
        }

        try {
            String content = new String(
                    file.getBytes(),
                    StandardCharsets.UTF_8
            );

            LogFile logFile = new LogFile(safeFilename, content);
            LogFile savedLog = logFileRepository.save(logFile);

            return new LogUploadResponse(
                    savedLog.getId(),
                    savedLog.getOriginalFilename(),
                    savedLog.getStatus(),
                    savedLog.getUploadedAt()
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded file",
                    exception
            );
        }
    }

    public List<LogUploadResponse> getAllLogs() {

        return logFileRepository.findAll()
                .stream()
                .map(log -> new LogUploadResponse(
                        log.getId(),
                        log.getOriginalFilename(),
                        log.getStatus(),
                        log.getUploadedAt()
                ))
                .toList();
    }

    public LogDetailsResponse getLogById(Long id) {

        LogFile log = logFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Log file not found with ID: " + id
                ));

        return new LogDetailsResponse(
                log.getId(),
                log.getOriginalFilename(),
                log.getStatus(),
                log.getUploadedAt(),
                log.getContent()
        );
    }
}
