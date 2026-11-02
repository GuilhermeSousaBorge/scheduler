package com.guilherme.scheduler.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidDataException extends BusinessException {
    public InvalidDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
