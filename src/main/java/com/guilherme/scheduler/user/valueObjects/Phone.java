package com.guilherme.scheduler.user.valueObjects;

public record Phone(String number) {

    public Phone {
        if (!validate(number)) {
            throw new IllegalArgumentException("Invalid Phone number");
        }
    }

    private boolean validate(String number) {
        return number != null && number.matches("^(?:(?:\\+|00)?(55)\\s?)?(?:\\(?([1-9][0-9])\\)?\\s?)?(?:((?:9\\d|[2-9])\\d{3})\\-?(\\d{4}))$");
    }
}
