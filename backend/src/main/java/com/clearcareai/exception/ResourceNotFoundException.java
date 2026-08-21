package com.clearcareai.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // convenience version: builds the message from the pieces
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " not found with " + fieldName + ": " + fieldValue);
    }
}
