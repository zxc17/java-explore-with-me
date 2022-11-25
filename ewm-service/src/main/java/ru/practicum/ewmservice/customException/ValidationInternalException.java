package ru.practicum.ewmservice.customException;

/**
 * Внутренняя ошибка приложения.
 */
public class ValidationInternalException extends RuntimeException {
    public ValidationInternalException(String message) {
        super(message);
    }
}
