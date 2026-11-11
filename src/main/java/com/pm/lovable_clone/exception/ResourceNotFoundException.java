package com.pm.lovable_clone.exception;
//Added custom exceptions:
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
