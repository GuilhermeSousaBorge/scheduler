package com.guilherme.scheduler.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidData extends BusinessException {
    public InvalidData(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
