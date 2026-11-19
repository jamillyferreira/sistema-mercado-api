package com.ferreiradev.sistemamercado.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Obrigatório ter e-mail ou username para logar")
        String identifier,

        @NotBlank(message = "Senha é obrigatória")// email ou username
        String password
) {
}
