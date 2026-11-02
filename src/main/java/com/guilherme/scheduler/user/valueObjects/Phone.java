package com.guilherme.scheduler.user.valueObjects;

import com.guilherme.scheduler.shared.exception.InvalidData;

public record Phone(String number) {

    public Phone {
        if (!validate(number)) {
            throw new InvalidData("Numero invalido");
        }
    }

    private boolean validate(String number) {
        return number != null && number.matches("^\\+\\d{13}$");
    }
}
