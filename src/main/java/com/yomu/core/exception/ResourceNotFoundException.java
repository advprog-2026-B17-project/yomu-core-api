package com.yomu.core.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}