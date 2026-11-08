package com.guilherme.scheduler.shared.config;

import com.guilherme.scheduler.user.model.enums.Role;

public record CurrentUser(Long id, Role role) {

}
