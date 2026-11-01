package com.guilherme.scheduler.user.valueObjects;

import org.springframework.security.crypto.password.PasswordEncoder;

public record Password(String value) {

    public static Password create(String raw, PasswordEncoder encoder) {
        if (raw == null || raw.length() < 8)
            throw new IllegalArgumentException("Password length must be at least 8 characters long");

        return new Password(encoder.encode(raw));
    }

    public static Password fromHash(String hash) {
        if (hash == null || hash.isBlank()) throw new IllegalArgumentException("Password hash cannot be null or blank");

        return new Password(hash);
    }

    public boolean matches(String raw, PasswordEncoder encoder) {
        return raw != null && encoder.matches(raw, this.value);
    }
}
