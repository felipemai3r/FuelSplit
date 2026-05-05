package com.fuelsplit.auth.dto;

import java.util.UUID;

public record AuthResponse(UUID userId, String token, String email) {}
