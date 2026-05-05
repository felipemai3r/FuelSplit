package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.CnhRequest;
import com.caronatracker.usersgroupsservice.dto.UserRequest;
import com.caronatracker.usersgroupsservice.dto.UserResponse;
import com.caronatracker.usersgroupsservice.entity.User;
import com.caronatracker.usersgroupsservice.exception.EntityNotFoundException;
import com.caronatracker.usersgroupsservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("E-mail já cadastrado: " + request.email());
        }
        User user = new User();
        user.setAuthUserId(request.authUserId());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        return toResponse(findById(id));
    }

    public UserResponse updateUser(UUID id, UserRequest request) {
        User user = findById(id);
        user.setName(request.name());
        user.setPhone(request.phone());
        return toResponse(userRepository.save(user));
    }

    public UserResponse updateCnh(UUID id, CnhRequest request) {
        if (request.cnhNumber() == null || request.cnhNumber().isBlank()) {
            throw new IllegalArgumentException("cnhNumber é obrigatório");
        }
        if (request.cnhExpiry() == null) {
            throw new IllegalArgumentException("cnhExpiry é obrigatório");
        }
        if (request.cnhExpiry().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("CNH vencida: cnhExpiry não pode ser anterior à data atual");
        }
        User user = findById(id);
        user.setCnhNumber(request.cnhNumber());
        user.setCnhExpiry(request.cnhExpiry());
        user.setHasCnh(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public boolean isValidDriver(UUID userId) {
        User user = findById(userId);
        return Boolean.TRUE.equals(user.getHasCnh())
                && user.getCnhExpiry() != null
                && !user.getCnhExpiry().isBefore(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para e-mail: " + email));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getAuthUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getHasCnh(),
                user.getCnhNumber(),
                user.getCnhExpiry(),
                user.getCreatedAt()
        );
    }
}
