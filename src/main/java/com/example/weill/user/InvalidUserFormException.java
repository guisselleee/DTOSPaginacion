package com.example.weill.user;

public class InvalidUserFormException extends RuntimeException {

    public InvalidUserFormException(String message) {
        super(message);
    }
}
