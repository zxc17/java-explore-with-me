package ru.practicum.ewmservice.customException;

/**
 * Запрашиваемый объект не найден.
 */
public class ValidationConflictException extends RuntimeException {
    public ValidationConflictException(String message) {
        super(message);
    }
}
