package com.job.challenge.application.exceptions;


import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public abstract class ServiceException extends RuntimeException {

    protected HttpStatus status;
    protected String errorCode;
    protected LocalDateTime timeStamp;

    public ServiceException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.timeStamp = LocalDateTime.now();
    }

    public ServiceException(String message, String errorCode, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.timeStamp = LocalDateTime.now();
    }
}
