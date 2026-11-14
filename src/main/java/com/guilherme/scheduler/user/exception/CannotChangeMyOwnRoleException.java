package com.guilherme.scheduler.user.exception;

import com.guilherme.scheduler.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CantChangeMyOwnRoleException extends BusinessException {
    public CantChangeMyOwnRoleException() {

        super("Nao é possivel alterar o proprio cargo", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
