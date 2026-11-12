package com.ferreiradev.sistemamercado.user.dto.response;


import com.ferreiradev.sistemamercado.user.enums.Role;

public record SignupResponse(
        String name,
        String email,
        String password,
        String businessType,
        Role role,
        boolean active
) {
}
