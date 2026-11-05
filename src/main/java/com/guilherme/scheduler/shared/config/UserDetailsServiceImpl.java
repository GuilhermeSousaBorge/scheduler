package com.guilherme.scheduler.shared.config;

import com.guilherme.scheduler.shared.exception.ResourceNotFoundException;
import com.guilherme.scheduler.user.model.valueObjects.Phone;
import com.guilherme.scheduler.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByPhone(new Phone(phone)).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
