package com.fuelsplit.auth;

import com.fuelsplit.auth.dto.AuthResponse;
import com.fuelsplit.auth.dto.LoginRequest;
import com.fuelsplit.auth.dto.RegisterRequest;
import com.fuelsplit.auth.entity.AuthUser;
import com.fuelsplit.auth.exception.EmailAlreadyExistsException;
import com.fuelsplit.auth.repository.AuthUserRepository;
import com.fuelsplit.auth.security.JwtService;
import com.fuelsplit.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository repository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private AuthUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AuthUser();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail("user@example.com");
        existingUser.setPasswordHash("hashedPassword");
        existingUser.setRole("USER");
    }

    @Test
    void register_success() {
        UUID userId = UUID.randomUUID();
        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(repository.save(any(AuthUser.class))).thenAnswer(inv -> {
            AuthUser u = inv.getArgument(0);
            u.setId(userId);
            return u;
        });
        when(jwtService.generateToken(eq(userId), eq("new@example.com"), eq("USER"))).thenReturn("token");

        AuthResponse response = authService.register(new RegisterRequest("new@example.com", "password123"));

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.token()).isEqualTo("token");
        assertThat(response.email()).isEqualTo("new@example.com");
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(repository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("user@example.com", "password123")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void register_shortPassword_throwsException() {
        assertThatThrownBy(() -> authService.register(new RegisterRequest("new@example.com", "12345")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_success() {
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq(existingUser.getId()), eq("user@example.com"), eq("USER"))).thenReturn("token");

        AuthResponse response = authService.login(new LoginRequest("user@example.com", "password123"));

        assertThat(response.userId()).isEqualTo(existingUser.getId());
        assertThat(response.token()).isEqualTo("token");
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrongpassword")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsException() {
        when(repository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@example.com", "password123")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
