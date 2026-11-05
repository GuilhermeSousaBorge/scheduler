package com.guilherme.scheduler.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Preencha o usuario") String phone,
        @NotBlank(message = "Preencha a senha") String password
) {
}
