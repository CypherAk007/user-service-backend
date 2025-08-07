package com.backend.ecommerceuserservice.exceptions;

public class UserAlreadyInDBException extends RuntimeException {
    public UserAlreadyInDBException(String message) {
        super(message);
    }
}
