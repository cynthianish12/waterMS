package com.utilitybilling.exception;

/** Raised when a requested entity cannot be found. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
