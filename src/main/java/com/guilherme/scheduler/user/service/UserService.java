package com.guilherme.scheduler.user.service;

import com.guilherme.scheduler.shared.exception.ResourceNotFoundException;
import com.guilherme.scheduler.user.controller.UserController;
import com.guilherme.scheduler.user.dto.CurrentUser;
import com.guilherme.scheduler.user.dto.UserRequestDTO;
import com.guilherme.scheduler.user.dto.UserResponseDTO;
import com.guilherme.scheduler.user.exception.PhoneAlreadyRegisteredException;
import com.guilherme.scheduler.user.mapper.UserMapper;
import com.guilherme.scheduler.user.model.User;
import com.guilherme.scheduler.user.model.enums.Role;
import com.guilherme.scheduler.user.repository.UserRepository;

import com.guilherme.scheduler.user.model.valueObjects.Password;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {

        Phone newPhone = new Phone(userRequestDTO.phone());

        if(userRepository.existsByPhone(newPhone)) throw new PhoneAlreadyRegisteredException();

        User user = new User(
                userRequestDTO.name(),
                newPhone,
                Password.create(userRequestDTO.password(), passwordEncoder));

        var createdUser = userRepository.save(user);

        return userMapper.responseDTO(createdUser);
    }

    public UserResponseDTO findUserById(Long id, CurrentUser currentUser) {

        if(!currentUser.role().canViewOtherProfiles() && !Objects.equals(currentUser.id(), id)) throw new ResourceNotFoundException("Usuario nao encontrado");

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        return userMapper.responseDTO(user);
    }
}
