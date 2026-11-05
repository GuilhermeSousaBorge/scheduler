package com.guilherme.scheduler.user.dto;

public record UserUpdateRequestDTO(
        String name,
        String password,
        String phone) {
}
