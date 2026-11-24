package com.ferreiradev.sistemamercado.category.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(min = 1, max = 150)
        @Pattern(regexp = ".*\\S.*", message = "Nome não pode ser vazio")
        String name,

        @Size(max = 255)
        String description
) {
}
