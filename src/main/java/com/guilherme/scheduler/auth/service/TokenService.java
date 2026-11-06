package com.guilherme.scheduler.auth.service;

import com.guilherme.scheduler.auth.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.expiration}")
    private Duration jwtExpiration;

    private final JwtEncoder jwtEncoder;

    public String generateToken(Authentication authentication) {

        Instant now = Instant.now();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("scheduler")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpiration.getSeconds()))
                .subject(userDetails.getId().toString())
                .claim("roles",roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Long getTokenExpiration(){
        return jwtExpiration.getSeconds();
    }
}
