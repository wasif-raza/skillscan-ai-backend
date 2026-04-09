package com.skillscan.ai.exception;

public class AIProcessingTimeoutException extends RuntimeException{
    public AIProcessingTimeoutException(String message, Throwable cause){
        super(message, cause);
    }
}
