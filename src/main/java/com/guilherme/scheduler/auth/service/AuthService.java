package com.guilherme.scheduler.auth.service;

import com.guilherme.scheduler.auth.dto.LoginRequestDto;
import com.guilherme.scheduler.auth.dto.LoginResponseDto;
import com.guilherme.scheduler.auth.config.CustomUserDetails;
import com.guilherme.scheduler.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.phone(), loginRequestDto.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserResponseDTO response = new UserResponseDTO(userDetails.getId(), userDetails.getName(), userDetails.getPhone());

        return new LoginResponseDto(response, tokenService.generateToken(authentication),tokenService.getTokenExpiration());
    }
}
