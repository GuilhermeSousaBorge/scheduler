package com.guilherme.scheduler.user.mapper;

import com.guilherme.scheduler.user.dto.UserResponseDTO;
import com.guilherme.scheduler.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO responseDTO(User user) {

        if (user == null) return null;

        return new UserResponseDTO(user.getId(), user.getName(), user.getPhone().number());
    }
}
