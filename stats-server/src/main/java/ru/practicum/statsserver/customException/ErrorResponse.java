package ru.practicum.statsserver.customException;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.statsserver.util.Constants.DATE_PATTERN;

@Getter
public class ErrorResponse {
    private final String status;  // Статус.
    private final String message; // Описание ошибки.
    private final String reason;  // Причина.
    private final String timestamp;

    public ErrorResponse(String status, String message, String reason) {
        this.status = status;
        this.message = message;
        this.reason = reason;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }
}
