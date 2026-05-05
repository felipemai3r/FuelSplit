package com.fuelsplit.auth.service;

import com.fuelsplit.auth.dto.AuthResponse;
import com.fuelsplit.auth.dto.LoginRequest;
import com.fuelsplit.auth.dto.RegisterRequest;
import com.fuelsplit.auth.entity.AuthUser;
import com.fuelsplit.auth.exception.EmailAlreadyExistsException;
import com.fuelsplit.auth.repository.AuthUserRepository;
import com.fuelsplit.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthUserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthUserRepository repository, JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.email());
        }
        AuthUser user = new AuthUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        repository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(user.getId(), token, user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = repository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(user.getId(), token, user.getEmail());
    }
}
