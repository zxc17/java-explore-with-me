package ru.practicum.ewmservice.customException;

/**
 * Запрашиваемый объект не найден.
 */
public class ValidationNotFoundException extends RuntimeException {
    public ValidationNotFoundException(String message) {
        super(message);
    }
}
