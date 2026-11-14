package com.guilherme.scheduler.user.exception;

import com.guilherme.scheduler.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CannotChangeMyOwnRoleException extends BusinessException {
    public CannotChangeMyOwnRoleException() {

        super("Nao é possivel alterar o proprio cargo", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
