package com.guilherme.scheduler.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank @Size(max = 150, message = "Nome nao pode conter mais de 150 caracteres") String name,
        @NotBlank @Size(min = 8, message = "Senha deve conter pelo menos 8 caracteres") String password,
        @NotBlank(message = "Telefone é obrigatorio") @Pattern(regexp = "^\\+\\d{13}$", message = "Telefone invalido") String phone) {
}
