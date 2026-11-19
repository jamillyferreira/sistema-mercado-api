package com.ferreiradev.sistemamercado.auth.dto;

import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
   public static final String BEARER = "Bearer";
}
