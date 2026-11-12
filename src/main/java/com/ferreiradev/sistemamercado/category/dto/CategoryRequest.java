package com.ferreiradev.sistemamercado.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Nome de categoria é obrigatório")
        @Size(min = 3, max = 150, message = "Deve ter pelo menos 3 caracteres")
        String name,

        @Size(max = 150, message = "Descrição deve ter no máximo 150 caracteres")
        String description
) {
}
