package com.ferreiradev.sistemamercado.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank(message = "Nome completo é obrigatório")
        String name,

        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 50, message = "Deve ter pelo menos 3 caracteres")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 255, message = "Senha precisa ter pelo menos 6 caracteres")
        String password
) {
}
