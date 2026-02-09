package com.job.challenge.application.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ServiceException{

    private static final HttpStatus HTTP_STATUS= HttpStatus.NOT_FOUND;
    private static final String DEFAULT_ERROR_CODE = "NOT_FOUND_EXCEPTION";

    public NotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE, HTTP_STATUS);
    }

    public NotFoundException(String message, String errorCode) {
        super(message, errorCode, HTTP_STATUS);
    }

    public NotFoundException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause, HTTP_STATUS);
    }
}
