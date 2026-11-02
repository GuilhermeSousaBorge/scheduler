package com.guilherme.scheduler.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFount extends BusinessException {

    public ResourceNotFount(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
