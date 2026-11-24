package com.ferreiradev.sistemamercado.category;

import com.ferreiradev.sistemamercado.category.dto.CategoryDetailsResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryRequest;
import com.ferreiradev.sistemamercado.category.dto.CategoryResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryUpdateRequest;
import com.ferreiradev.sistemamercado.category.mapper.CategoryMapper;
import com.ferreiradev.sistemamercado.common.exception.BusinessException;
import com.ferreiradev.sistemamercado.common.exception.NotFoundException;
import com.ferreiradev.sistemamercado.config.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
   private final CategoryRepository categoryRepository;
   private final CurrentUser currentUser;
   private final CategoryMapper mapper;

   @Transactional
   @PreAuthorize("hasRole('ADMIN')")
   public CategoryResponse create(CategoryRequest request) {
      log.info("Tentativa criar categoria: name={}", request.name());
      UUID businessId = currentUser.getBusinessId();
      if (categoryRepository.existsByNameIgnoreCaseAndBusinessId(request.name(), businessId)) {
         log.warn("Criação falhou: categoria já existente com esse nome: {}", request.name());
         throw new BusinessException("Nome de categoria já existe");
      }
      Category category = new Category();
      category.setBusinessId(businessId);
      category.setName(request.name());
      category.setDescription(request.description());

      Category savedCategory = categoryRepository.save(category);
      log.info("Categoria criada com sucesso: id={}, name={}", savedCategory.getId(), savedCategory.getName());
      return mapper.toResponse(savedCategory);
   }

   // edicao parcial
   @Transactional
   @PreAuthorize("hasRole('ADMIN')")
   public CategoryDetailsResponse update(UUID id, CategoryUpdateRequest request) {
      log.info("Tentativa de atualizar categoria: id={}, name={}", id, request.name());
      UUID businessId = currentUser.getBusinessId();

      Category category = categoryRepository.findByIdAndBusinessId(id, businessId)
              .orElseThrow(() -> {
                 log.warn("Categoria não encontrada: id={}, name={}", id, request.name());
                 return new NotFoundException("Categoria não encontrada");
              });

      if (request.name() != null) {
         if (categoryRepository.existsByNameIgnoreCaseAndBusinessIdAndIdNot(request.name(), businessId, id)) {
            log.warn("Atualização falhou: categoria já existente com esse nome:  {}", request.name());
            throw new BusinessException("Nome de categoria já existe");
         }
         category.setName(request.name());
      }
      if (request.description() != null) {
         category.setDescription(request.description());
      }
      Category savedCategory = categoryRepository.saveAndFlush(category);
      log.info("Categoria atualizada: id={}, novo nome={}, nova descricao={}",
              savedCategory.getId(), savedCategory.getName(), savedCategory.getDescription());
      return mapper.toDetailResponse(savedCategory);
   }

   // Admin e Employee podem listar
   @Transactional(readOnly = true)
   public Page<CategoryResponse> findAll(String name, Pageable pageable) {
      log.info("consultando categorias: name={}", name);
      UUID businessId = currentUser.getBusinessId();

      Page<Category> page = (name == null || name.isBlank())
              ? categoryRepository.findAllByBusinessId(businessId, pageable)
              : categoryRepository.findAllByNameContainingIgnoreCaseAndBusinessId(name, businessId, pageable);

      log.info("Busca retornada");
      return page.map(mapper::toResponse);
   }

   @Transactional(readOnly = true)
   public CategoryResponse findById(UUID id) {
      log.info("consultando categoria: id={}", id);

      Category category = categoryRepository.findByIdAndBusinessId(id, currentUser.getBusinessId())
              .orElseThrow(() -> {
                 log.warn("Busca falhou: categoria não encontrada com id={}", id);
                 return new NotFoundException("Categoria não encontrada");
              });

      log.info("Busca retornada: id={}, name={}", category.getId(), category.getName());
      return mapper.toResponse(category);
   }

   // TODO: deletar categoria se nao tiver produto cadastrado


}
