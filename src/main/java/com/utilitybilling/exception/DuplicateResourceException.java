package com.utilitybilling.exception;

/** Raised when a unique business key already exists. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
