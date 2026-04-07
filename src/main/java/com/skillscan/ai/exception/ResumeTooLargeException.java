package com.skillscan.ai.exception;

import org.springframework.http.HttpStatus;

public class ResumeTooLargeException extends BaseException {
    public ResumeTooLargeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}