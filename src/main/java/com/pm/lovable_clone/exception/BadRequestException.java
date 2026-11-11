package com.pm.lovable_clone.exception;
//Added custom exceptions:
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
