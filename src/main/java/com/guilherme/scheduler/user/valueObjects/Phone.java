package com.guilherme.scheduler.user.valueObjects;

public record Phone(String number) {

    public Phone {
        if (!validate(number)) {
            throw new IllegalArgumentException("Invalid Phone number");
        }
    }

    private boolean validate(String number) {
        return number != null && number.matches("^\\+d{2}\\d{2}\\d{9}");
    }
}
