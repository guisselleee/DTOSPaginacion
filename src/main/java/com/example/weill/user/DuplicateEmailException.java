package com.example.weill.user;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email is already registered: " + email);
    }
}
