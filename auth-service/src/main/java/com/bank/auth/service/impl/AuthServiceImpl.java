package com.bank.auth.service.impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.auth.dto.request.LoginRequest;
import com.bank.auth.dto.request.RegisterRequest;
import com.bank.auth.dto.response.LoginResponse;
import com.bank.auth.dto.response.RegisterResponse;
import com.bank.auth.role.entity.Role;
import com.bank.auth.role.repository.RoleRepository;
import com.bank.auth.service.AuthService;
import com.bank.auth.user.entity.User;
import com.bank.auth.user.repository.UserRepository;
import com.bank.security.jwt.service.JwtService;
import com.bank.security.user.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()));

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        String accessToken =
                jwtService.generateAccessToken(principal);

        String refreshToken =
                jwtService.generateRefreshToken(principal);

        Instant expiresAt =
                jwtService.extractExpiration(accessToken);

        return new LoginResponse(
                accessToken,
                refreshToken,
                expiresAt,
                "Bearer");
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {

            throw new IllegalArgumentException(
                    "Username already exists: "
                            + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new IllegalArgumentException(
                    "Email already exists: "
                            + request.getEmail());
        }

        Role userRole =
                roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Default role ROLE_USER not found"));

        User user =
                User.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()))
                        .enabled(true)
                        .accountNonLocked(true)
                        .accountNonExpired(true)
                        .credentialsNonExpired(true)
                        .failedLoginAttempts(0)
                        .roles(
                                new HashSet<>(
                                        Set.of(userRole)))
                        .build();

        User savedUser =
                userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail());
    }

    @Override
    public void logout(String accessToken) {

        // JWT is stateless.
        // Token revocation can be implemented later.
    }

    @Override
    public LoginResponse refresh(String refreshToken) {

        throw new UnsupportedOperationException(
                "Refresh token flow not implemented yet");
    }
}