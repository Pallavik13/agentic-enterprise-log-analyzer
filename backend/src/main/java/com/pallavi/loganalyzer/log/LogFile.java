package com.pallavi.loganalyzer.log;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "log_files")
public class LogFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    protected LogFile() {
    }

    public LogFile(String originalFilename, String content) {
        this.originalFilename = originalFilename;
        this.content = content;
        this.status = "RECEIVED";
    }

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
