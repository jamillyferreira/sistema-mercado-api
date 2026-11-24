package com.ferreiradev.sistemamercado.category.mapper;

import com.ferreiradev.sistemamercado.category.Category;
import com.ferreiradev.sistemamercado.category.dto.CategoryDetailsResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryResponse;
import com.ferreiradev.sistemamercado.common.audit.mapper.BaseAuditMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BaseAuditMapper.class)
public interface CategoryMapper {
   CategoryResponse toResponse(Category category);

   @Mapping(target = "audit", source = ".")
   CategoryDetailsResponse toDetailResponse(Category category);
}
