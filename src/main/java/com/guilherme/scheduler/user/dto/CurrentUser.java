package com.guilherme.scheduler.user.dto;

import com.guilherme.scheduler.user.model.enums.Role;

public record CurrentUser(Long id, Role role) {

}
