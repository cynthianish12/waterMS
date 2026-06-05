package com.utilitybilling.exception;

/** Raised when a domain rule is violated. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
