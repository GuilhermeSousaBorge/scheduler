package com.guilherme.scheduler.user.exception;

import com.guilherme.scheduler.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("Usuário não encontrado", HttpStatus.NOT_FOUND);
    }
}
