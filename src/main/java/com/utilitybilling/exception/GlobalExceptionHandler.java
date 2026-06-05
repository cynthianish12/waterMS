package com.utilitybilling.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/** Converts validation, security, persistence, and business failures into clear API errors. */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({BusinessException.class, ConstraintViolationException.class})
    ResponseEntity<ErrorResponse> business(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null && ex.getMessage().contains("PaymentMethod")
                ? "paymentMethod must be valid: CASH, MOMO, BANK, CARD."
                : "Invalid request body.";
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ErrorResponse> duplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> missing(ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> credentials(BadCredentialsException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid credentials", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest request) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank() ? "Access denied" : ex.getMessage();
        return error(HttpStatus.FORBIDDEN, message, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> constraint(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
                && ex.getMostSpecificCause().getMessage().contains("uk_reading_meter_month_year")
                ? "This meter already has a reading for the selected month/year."
                : "Database constraint violation";
        return error(HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(), status.value(), status.name(), message, request.getRequestURI()));
    }
}
