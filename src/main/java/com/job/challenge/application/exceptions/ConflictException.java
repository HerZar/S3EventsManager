package com.job.challenge.application.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends ServiceException{

    private static final HttpStatus HTTP_STATUS= HttpStatus.CONFLICT;
    private static final String DEFAULT_ERROR_CODE = "CONFLICT_EXCEPTION";

    public ConflictException(String message) {
        super(message, DEFAULT_ERROR_CODE, HTTP_STATUS);
    }

    public ConflictException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }

    public ConflictException(String message, String errorCode, Throwable cause, HttpStatus status) {
        super(message, errorCode, cause, status);
    }
}
