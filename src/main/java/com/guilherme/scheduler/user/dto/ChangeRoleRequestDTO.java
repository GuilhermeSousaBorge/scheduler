package com.guilherme.scheduler.user.dto;

import com.guilherme.scheduler.user.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequestDTO(
        @NotNull(message = "Campo funçao nao pode ser vazio") Role role
) {
}
