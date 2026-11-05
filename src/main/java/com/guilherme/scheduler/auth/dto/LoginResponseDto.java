package com.guilherme.scheduler.auth.dto;

import com.guilherme.scheduler.user.dto.UserResponseDTO;

import java.time.Duration;

public record LoginResponseDto(
        UserResponseDTO user,
        String token,
        Duration expiresIn
) {
}
