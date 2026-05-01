package com.skillscan.ai.exception;

import org.springframework.http.HttpStatus;

public class JwtValidationException extends BaseException {

    public JwtValidationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}