package com.ferreiradev.sistemamercado.category.dto;

import com.ferreiradev.sistemamercado.common.audit.dto.AuditResponse;

import java.util.UUID;

public record CategoryDetailsResponse(
        UUID id,
        String name,
        String description,
        AuditResponse audit

) {
}
