package com.guilherme.scheduler.user.exception;

import com.guilherme.scheduler.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PhoneAlreadyRegisteredException extends BusinessException {

    public PhoneAlreadyRegisteredException() {
        super("Telefone ja cadastrado", HttpStatus.CONFLICT);
    }
}
