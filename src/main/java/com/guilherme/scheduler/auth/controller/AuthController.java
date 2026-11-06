package com.guilherme.scheduler.auth.controller;

import com.guilherme.scheduler.auth.dto.LoginRequestDto;
import com.guilherme.scheduler.auth.dto.LoginResponseDto;
import com.guilherme.scheduler.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private AuthService authService;

    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return  ResponseEntity.ok(authService.login(loginRequestDto));
    }
}
