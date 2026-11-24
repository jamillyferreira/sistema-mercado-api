package com.ferreiradev.sistemamercado.category.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String description,

        Instant createdAt
) {
}
