package com.guilherme.scheduler.shared.exception;

import org.springframework.http.HttpStatus;

public class PhoneAlreadyRegistered extends BusinessException {

    protected PhoneAlreadyRegistered(String message) {
        super("Telefone ja cadastrado", HttpStatus.CONFLICT);
    }
}
