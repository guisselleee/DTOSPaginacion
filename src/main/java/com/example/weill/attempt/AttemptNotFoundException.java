package com.example.weill.attempt;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AttemptNotFoundException extends RuntimeException {

    public AttemptNotFoundException(long attemptId) {
        super("Attempt not found: " + attemptId);
    }
}
