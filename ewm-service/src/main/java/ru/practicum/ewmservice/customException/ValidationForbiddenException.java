package ru.practicum.ewmservice.customException;

/**
 * Нет прав на выполнение запроса.
 */
public class ValidationForbiddenException extends RuntimeException {
    public ValidationForbiddenException(String message) {
        super(message);
    }
}
