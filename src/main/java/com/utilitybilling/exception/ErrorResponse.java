package com.utilitybilling.exception;

import java.time.LocalDateTime;

/** Standard error response returned by the global exception handler. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}
