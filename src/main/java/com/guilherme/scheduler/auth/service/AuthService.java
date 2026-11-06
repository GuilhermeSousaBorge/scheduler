package com.guilherme.scheduler.auth.service;

import com.guilherme.scheduler.auth.dto.LoginRequestDto;
import com.guilherme.scheduler.auth.dto.LoginResponseDto;
import com.guilherme.scheduler.shared.exception.ResourceNotFoundException;
import com.guilherme.scheduler.user.dto.UserResponseDTO;
import com.guilherme.scheduler.user.model.User;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import com.guilherme.scheduler.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.expiration}")
    private Long expiration;

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.phone(), loginRequestDto.password())
        );

        User user = userRepository.findByPhone(new Phone(loginRequestDto.phone())).orElseThrow(() ->
                new ResourceNotFoundException("Usuario nao encontrado"));

        UserResponseDTO response = new UserResponseDTO(user.getId(), user.getName(), user.getPhone().number());
        return new LoginResponseDto(response, tokenService.generateToken(authentication),expiration);
    }
}
