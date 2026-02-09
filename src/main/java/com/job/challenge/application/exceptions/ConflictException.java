package com.job.challenge.application.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends ServiceException{

    private static final HttpStatus HTTP_STATUS= HttpStatus.CONFLICT;
    private static final String DEFAULT_ERROR_CODE = "CONFLICT_EXCEPTION";

    public ConflictException(String message) {
        super(message, DEFAULT_ERROR_CODE, HTTP_STATUS);
    }

    public ConflictException(String message, String errorCode) {
        super(message, errorCode, HTTP_STATUS);
    }

    public ConflictException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause, HTTP_STATUS);
    }
}
