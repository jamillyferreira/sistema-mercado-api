package com.ferreiradev.sistemamercado.user.dto.response;

import com.ferreiradev.sistemamercado.user.enums.Role;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

// Formato temporario, depois remover email
@Builder
public record UserResponse(
        UUID id,
        String name,
        String email,
        String username,
        String phone,
        Role role,
        boolean active,
        UUID businessId,
        Instant createdAt,
        Instant updatedAt
) {
}
