package com.guilherme.scheduler.user.service;

import com.guilherme.scheduler.user.exception.CannotChangeMyOwnRoleException;
import com.guilherme.scheduler.user.exception.UserNotFoundException;
import com.guilherme.scheduler.shared.config.CurrentUser;
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

import java.util.List;
import java.util.Objects;

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

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id, CurrentUser currentUser) {

        if(!currentUser.role().canViewOtherProfiles() && !Objects.equals(currentUser.id(), id)) throw new UserNotFoundException();

        User user = getUser(id);
        return userMapper.responseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::responseDTO).toList();
    }

    @Transactional
    public void changeRole(Long id, Role role, CurrentUser currentUser) {
        if(id.equals(currentUser.id())) throw new CannotChangeMyOwnRoleException();
        User user = getUser(id);
        user.changeRole(role);
    }

    private User getUser(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }
}
