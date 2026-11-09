package com.guilherme.scheduler.user.config;

import com.guilherme.scheduler.user.model.User;
import com.guilherme.scheduler.user.model.enums.Role;
import com.guilherme.scheduler.user.model.valueObjects.Password;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import com.guilherme.scheduler.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.StringJoiner;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args){

        boolean exist = userRepository.existsByRole(Role.ADMIN);

        if (exist) return;
        StringJoiner stringJoiner = new StringJoiner(", ");

        if(adminName.isBlank()) stringJoiner.add("ADMIN_NAME");
        if(adminPhone.isBlank()) stringJoiner.add("ADMIN_PHONE");
        if(adminPassword.isBlank()) stringJoiner.add("ADMIN_PASSWORD");

        if(stringJoiner.length() > 0) throw new IllegalStateException("Nenhum admin cadastrado. Defina a(s) variavel(eis): " + stringJoiner);

        User user = new User(adminName, new Phone(adminPhone), Password.create(adminPassword, passwordEncoder));
        user.changeRole(Role.ADMIN);
        userRepository.save(user);
        log.info("Usuario admin criado: Nome '{}' e telefone '{}'", adminName, adminPhone);
    }
}
