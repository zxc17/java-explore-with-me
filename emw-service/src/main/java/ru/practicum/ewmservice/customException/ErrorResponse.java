package ru.practicum.ewmservice.customException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponse {
    String status;  // Статус.
    String message; // Описание ошибки.
    String reason;  // Причина.
    String timestamp;

    public ErrorResponse(String status, String message, String reason) {
        this.status = status;
        this.message = message;
        this.reason = reason;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

