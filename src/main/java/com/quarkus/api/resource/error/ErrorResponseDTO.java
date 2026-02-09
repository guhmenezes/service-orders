package com.quarkus.api.resource.error;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ErrorResponseDTO {
    public LocalDateTime timestamp;
    public int status;
    public String error;
    public String message;
    public List<Map<String, String>> details;

    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ErrorResponseDTO(int status, String error, String message, List<Map<String, String>> details) {
        this(status, error, message);
        this.details = details;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<Map<String, String>> getDetails() { return details; }
    public void setDetails(List<Map<String, String>> details) { this.details = details; }
}