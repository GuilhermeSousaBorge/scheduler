package com.guilherme.scheduler.user.valueObjects;

import com.guilherme.scheduler.shared.exception.InvalidDataException;

public record Phone(String number) {

    public Phone {
        if (!validate(number)) {
            throw new InvalidDataException("Numero invalido");
        }
    }

    private boolean validate(String number) {
        return number != null && number.matches("^\\+\\d{13}$");
    }
}
