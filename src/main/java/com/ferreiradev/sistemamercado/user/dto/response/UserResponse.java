package com.ferreiradev.sistemamercado.user.dto.response;

import com.ferreiradev.sistemamercado.user.enums.Role;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        boolean active
) {
}
