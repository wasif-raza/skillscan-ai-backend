package com.skillscan.ai.exception;

import org.springframework.http.HttpStatus;

public class ResumeParsingException extends BaseException {
    public ResumeParsingException(String message){
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
