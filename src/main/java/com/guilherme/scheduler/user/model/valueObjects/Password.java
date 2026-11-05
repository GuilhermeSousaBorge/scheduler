package com.guilherme.scheduler.user.model.valueObjects;

import com.guilherme.scheduler.shared.exception.InvalidDataException;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class Password {

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password create(String raw, PasswordEncoder encoder) {
        if(raw == null || raw.length() < 8) throw new InvalidDataException("Senha: minimo 8 caracteres");

        return new Password(encoder.encode(raw));
    }

    public static Password fromHash(String hash){
        if(hash == null || hash.isBlank()) throw new IllegalStateException("Senha nula ou em branco ao consultar o banco");

        return new Password(hash);
    }

    public boolean matches(String raw, PasswordEncoder encoder) {
        return raw != null && encoder.matches(raw, this.value);
    }
}
