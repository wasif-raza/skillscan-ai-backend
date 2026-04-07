package com.skillscan.ai.exception;

import org.springframework.http.HttpStatus;

public class ResumeNotFoundException extends BaseException {
    public ResumeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}