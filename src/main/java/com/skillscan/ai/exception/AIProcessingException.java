package com.skillscan.ai.exception;

public class AIProcessingException extends RuntimeException{
    public  AIProcessingException(String message, Throwable cause){
        super(message,cause);
    }
}
