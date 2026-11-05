package com.guilherme.scheduler.auth.dto;

import com.guilherme.scheduler.user.dto.UserResponseDTO;

public record LoginResponseDto(
        UserResponseDTO user,
        String token
) {
}
