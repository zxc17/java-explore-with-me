package ru.practicum.statsserver.customException;

/**
 * Переданы некорректные данные.
 */
public class ValidationDataException extends RuntimeException {
    public ValidationDataException(String message) {
        super(message);
    }
}
