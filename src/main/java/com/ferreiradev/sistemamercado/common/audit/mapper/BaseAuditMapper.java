package com.ferreiradev.sistemamercado.common.audit.mapper;

import com.ferreiradev.sistemamercado.common.audit.BaseAudit;
import com.ferreiradev.sistemamercado.common.audit.dto.AuditResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BaseAuditMapper {
   AuditResponse toResponse(BaseAudit entity);
}
