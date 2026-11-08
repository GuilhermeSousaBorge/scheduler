package com.guilherme.scheduler.shared.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException{
    public UserNotFoundException() {
        super("Usuário não encontrado", HttpStatus.NOT_FOUND);
    }
}
