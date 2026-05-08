package com.skillscan.ai.exception;

import org.springframework.http.HttpStatus;

public class TokenBlacklistException extends BaseException {

    public TokenBlacklistException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}