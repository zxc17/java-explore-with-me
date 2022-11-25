package ru.practicum.ewmservice.customException;

/**
 * Переданы некорректные данные.
 */
public class ValidationDataException extends RuntimeException {
    public ValidationDataException(String message) {
        super(message);
    }
}
