package com.guilherme.scheduler.user.controller;

import com.guilherme.scheduler.user.dto.CurrentUser;
import com.guilherme.scheduler.user.dto.UserRequestDTO;
import com.guilherme.scheduler.user.dto.UserResponseDTO;
import com.guilherme.scheduler.user.model.enums.Role;
import com.guilherme.scheduler.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO response = userService.createUser(userRequestDTO);


       return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.id()).toUri()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.parseLong(jwt.getSubject());
        List<String> roles = jwt.getClaimAsStringList("roles");
        CurrentUser currentUser = new CurrentUser(userId, Role.valueOf(roles.getFirst()));
        return ResponseEntity.ok(userService.findUserById(id, currentUser));
    }
}


