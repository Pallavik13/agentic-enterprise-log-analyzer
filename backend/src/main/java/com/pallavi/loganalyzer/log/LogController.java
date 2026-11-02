package com.pallavi.loganalyzer.log;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<LogUploadResponse> uploadLog(
            @RequestParam("file") MultipartFile file) {

        LogUploadResponse response = logService.uploadLog(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<LogUploadResponse>> getAllLogs() {

        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogDetailsResponse> getLogById(
            @PathVariable Long id) {

        return ResponseEntity.ok(logService.getLogById(id));
    }
}
