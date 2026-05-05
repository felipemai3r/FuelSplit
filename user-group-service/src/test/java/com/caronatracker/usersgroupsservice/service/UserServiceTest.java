package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.CnhRequest;
import com.caronatracker.usersgroupsservice.dto.UserRequest;
import com.caronatracker.usersgroupsservice.dto.UserResponse;
import com.caronatracker.usersgroupsservice.entity.User;
import com.caronatracker.usersgroupsservice.exception.EntityNotFoundException;
import com.caronatracker.usersgroupsservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        UserRequest request = new UserRequest(UUID.randomUUID(), "João Silva", "joao@email.com", "11999999999");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

        User saved = buildUser(UUID.randomUUID(), "João Silva", "joao@email.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("João Silva", response.name());
        assertEquals("joao@email.com", response.email());
    }

    @Test
    void createUser_duplicateEmail() {
        UserRequest request = new UserRequest(UUID.randomUUID(), "João", "joao@email.com", null);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalStateException.class, () -> userService.createUser(request));
    }

    @Test
    void updateCnh_validData() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "João", "joao@email.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        CnhRequest request = new CnhRequest("12345678900", LocalDate.now().plusYears(2));
        UserResponse response = userService.updateCnh(userId, request);

        assertTrue(response.hasCnh());
        assertEquals("12345678900", response.cnhNumber());
    }

    @Test
    void updateCnh_missingCnhNumber() {
        CnhRequest request = new CnhRequest(null, LocalDate.now().plusYears(1));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateCnh(UUID.randomUUID(), request));
    }

    @Test
    void updateCnh_expiredCnh() {
        CnhRequest request = new CnhRequest("12345678900", LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateCnh(UUID.randomUUID(), request));
    }

    @Test
    void isValidDriver_withValidCnh() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "João", "joao@email.com");
        user.setHasCnh(true);
        user.setCnhNumber("12345678900");
        user.setCnhExpiry(LocalDate.now().plusYears(1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertTrue(userService.isValidDriver(userId));
    }

    @Test
    void isValidDriver_withExpiredCnh() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "João", "joao@email.com");
        user.setHasCnh(true);
        user.setCnhNumber("12345678900");
        user.setCnhExpiry(LocalDate.now().minusDays(1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertFalse(userService.isValidDriver(userId));
    }

    @Test
    void isValidDriver_withoutCnh() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "João", "joao@email.com");
        user.setHasCnh(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertFalse(userService.isValidDriver(userId));
    }

    private User buildUser(UUID id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setHasCnh(false);
        return user;
    }
}
