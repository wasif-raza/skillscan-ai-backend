package com.skillscan.ai.exception;

import org.springframework.http.HttpStatus;

public class TokenHashingException extends BaseException{

    public TokenHashingException(String message){
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
