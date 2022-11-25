package ru.practicum.ewmservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewmservice.customException.ErrorResponse;
import ru.practicum.ewmservice.customException.ValidationConflictException;
import ru.practicum.ewmservice.customException.ValidationDataException;
import ru.practicum.ewmservice.customException.ValidationForbiddenException;
import ru.practicum.ewmservice.customException.ValidationNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationData(final ValidationDataException e) {
        String status = HttpStatus.BAD_REQUEST.toString();
        String message = e.getMessage();
        String reason = "For the requested operation the conditions are not met.";
        log.warn("400 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        String status = HttpStatus.BAD_REQUEST.toString();
        String reason = "For the requested operation the conditions are not met.";
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList()).toString();
        log.warn("400 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(final ConstraintViolationException e) {
        String status = HttpStatus.BAD_REQUEST.toString();
        String reason = "For the requested operation the conditions are not met.";
        String message = e.getMessage();
        log.warn("400 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleValidationSpring(final ValidationForbiddenException e) {
        String status = HttpStatus.FORBIDDEN.toString();
        String message = e.getMessage();
        String reason = "For the requested operation the conditions are not met.";
        log.warn("403 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleValidationNotFound(final ValidationNotFoundException e) {
        String status = HttpStatus.NOT_FOUND.toString();
        String message = e.getMessage();
        String reason = "The required object was not found.";
        log.warn("404 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(final DataIntegrityViolationException e) {
        String status = HttpStatus.CONFLICT.toString();
        String message = (e.getRootCause() == null) ? e.getMessage() : e.getRootCause().getMessage();
        String reason = "Integrity constraint has been violated.";
        log.warn("409 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationConflict(final ValidationConflictException e) {
        String status = HttpStatus.CONFLICT.toString();
        String message = e.getMessage();
        String reason = "Integrity constraint has been violated.";
        log.warn("409 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse defaultHandle(final Exception e) {
        String status = HttpStatus.INTERNAL_SERVER_ERROR.toString();
        String message = e.getMessage();
        String reason = "Error occurred.";
        log.warn("500 {}", message, e);
        return new ErrorResponse(status, message, reason);
    }

}
