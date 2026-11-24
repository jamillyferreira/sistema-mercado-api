package com.ferreiradev.sistemamercado.category;

import com.ferreiradev.sistemamercado.category.dto.CategoryDetailsResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryRequest;
import com.ferreiradev.sistemamercado.category.dto.CategoryResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryUpdateRequest;
import com.ferreiradev.sistemamercado.category.mapper.CategoryMapper;
import com.ferreiradev.sistemamercado.common.exception.BusinessException;
import com.ferreiradev.sistemamercado.common.exception.NotFoundException;
import com.ferreiradev.sistemamercado.config.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
   @Mock
   private CategoryRepository categoryRepository;

   @Mock
   private CurrentUser currentUser;

   @Mock
   private CategoryMapper mapper;

   @InjectMocks
   private CategoryService service;

   private final UUID businessId = UUID.randomUUID();
   private final Pageable pageable = PageRequest.of(0, 10);

   @Test
   void create_DeveSalvarCategoriaComSucessoEComBusiness() {
      var request = new CategoryRequest("Bebidas", "Refrigerante, Sucos, Aguas");
      var response = new CategoryResponse(UUID.randomUUID(), "Bebidas", "Refrigerante, Sucos, Aguas", Instant.now());

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.existsByNameIgnoreCaseAndBusinessId(request.name(), businessId)).thenReturn(false);
      when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
      when(mapper.toResponse(any(Category.class))).thenReturn(response);

      CategoryResponse result = service.create(request);
      assertThat(result).isEqualTo(response);

      ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
      verify(categoryRepository).save(captor.capture());
      assertThat(captor.getValue().getBusinessId()).isEqualTo(businessId);
      assertThat(captor.getValue().getName()).isEqualTo(request.name());
      assertThat(captor.getValue().getDescription()).isEqualTo(request.description());

   }

   @Test
   void create_NomeExistente_NaoDeveSalvarCategoria() {
      var request = new CategoryRequest("Bebidas", "Refrigerante, Sucos, Aguas");

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.existsByNameIgnoreCaseAndBusinessId(request.name(), businessId)).thenReturn(true);

      assertThatThrownBy(() -> service.create(request))
              .isInstanceOf(BusinessException.class)
              .hasMessage("Nome de categoria já existe");

      verifyNoInteractions(mapper);
      verify(categoryRepository, never()).save(any(Category.class));
   }

   @Test
   void create_DeveSalvarCategoria_QuandoDescricaoForVazio() {
      var request = new CategoryRequest("Bebidas", null);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.existsByNameIgnoreCaseAndBusinessId(request.name(), businessId)).thenReturn(false);
      when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

      service.create(request);

      ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
      verify(categoryRepository).save(captor.capture());

      assertThat(captor.getValue().getName()).isEqualTo(request.name());
      assertThat(captor.getValue().getDescription()).isNull();
   }

   @Test
   void update_DeveAtualizarCategoriaComNovoNome_MantendoDescricao() {
      UUID id = UUID.randomUUID();
      Category existingCategory = new Category(businessId, "Bebidas", "Refrigerante, Sucos, Aguas");
      var request = new CategoryUpdateRequest("Sucos", null);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.existsByNameIgnoreCaseAndBusinessIdAndIdNot(request.name(), businessId, id)).thenReturn(false);
      when(categoryRepository.saveAndFlush(any(Category.class))).thenAnswer(i -> i.getArgument(0));

      service.update(id, request);

      ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
      verify(categoryRepository).saveAndFlush(captor.capture());

      assertThat(captor.getValue().getBusinessId()).isEqualTo(businessId);
      assertThat(captor.getValue().getName()).isEqualTo(request.name());
      assertThat(captor.getValue().getDescription()).isEqualTo("Refrigerante, Sucos, Aguas"); // descricao nao mudou
   }

   @Test
   void update_DeveAtualizarDescricaoDaCategoria_MantendoNome() {
      UUID id = UUID.randomUUID();
      Category existingCategory = new Category(businessId, "Bebidas", "Refrigerante, Sucos, Aguas");
      var request = new CategoryUpdateRequest(null, "Nova descrição");

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.saveAndFlush(any(Category.class))).thenAnswer(i -> i.getArgument(0));

      service.update(id, request);

      ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
      verify(categoryRepository).saveAndFlush(captor.capture());

      assertThat(captor.getValue().getBusinessId()).isEqualTo(businessId);
      assertThat(captor.getValue().getName()).isEqualTo("Bebidas"); // Nome nao mudou
      assertThat(captor.getValue().getDescription()).isEqualTo(request.description());
   }

   @Test
   void update_DeveAtualizarNomeEDescricaoDaCategoria() {
      UUID id = UUID.randomUUID();
      Category existingCategory = new Category(businessId, "Carns", "Carne, Frango, Porco");
      var request = new CategoryUpdateRequest("Carnes", "Nova descrição");
      var response = new CategoryDetailsResponse(id, "Carnes", "Nova descrição", null);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.existsByNameIgnoreCaseAndBusinessIdAndIdNot(request.name(), businessId, id)).thenReturn(false);
      when(categoryRepository.saveAndFlush(any(Category.class))).thenAnswer(i -> i.getArgument(0));
      when(mapper.toDetailResponse(any(Category.class))).thenReturn(response);

      CategoryDetailsResponse result = service.update(id, request);
      assertThat(result).isEqualTo(response);

      ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
      verify(categoryRepository).saveAndFlush(captor.capture());

      assertThat(captor.getValue().getBusinessId()).isEqualTo(businessId);
      assertThat(captor.getValue().getName()).isEqualTo(request.name());
      assertThat(captor.getValue().getDescription()).isEqualTo(request.description());
   }

   @Test
   void update_CategoriaNaoEncontrada_DeveLancarNotFoundException() {
      UUID id = UUID.randomUUID();
      var request = new CategoryUpdateRequest("Sucos", null);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.update(id, request))
              .isInstanceOf(NotFoundException.class)
              .hasMessage("Categoria não encontrada");

      verifyNoInteractions(mapper);
      verify(categoryRepository, never()).saveAndFlush(any(Category.class));
      verify(categoryRepository, never()).existsByNameIgnoreCaseAndBusinessIdAndIdNot(any(), any(), any());
   }

   @Test
   void update_NomeJaUsado_NaoDeveSalvar() {
      UUID id = UUID.randomUUID();
      Category existingCategory = new Category(businessId, "Bebidas", "Refrigerante, Sucos, Aguas");
      var request = new CategoryUpdateRequest("Sucos", null);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.existsByNameIgnoreCaseAndBusinessIdAndIdNot(request.name(), businessId, id)).thenReturn(true);

      assertThatThrownBy(() -> service.update(id, request))
              .isInstanceOf(BusinessException.class)
              .hasMessage("Nome de categoria já existe");

      verifyNoInteractions(mapper);
      verify(categoryRepository, never()).saveAndFlush(any(Category.class));
   }

   @Test
   void update_RequestSemCampos_NaoDeveAlterarNada() {
      UUID id = UUID.randomUUID();
      Category existingCategory = new Category(businessId, "Sucos", "Refrigerante, Sucos, Aguas");
      var request = new CategoryUpdateRequest(null, null);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.saveAndFlush(any(Category.class))).thenAnswer(i -> i.getArgument(0));

      service.update(id, request);
      ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
      verify(categoryRepository).saveAndFlush(captor.capture());

      assertThat(captor.getValue().getBusinessId()).isEqualTo(businessId);
      assertThat(captor.getValue().getName()).isEqualTo("Sucos");
      assertThat(captor.getValue().getDescription()).isEqualTo("Refrigerante, Sucos, Aguas");

   }

   @Test
   void findAll_DeveFiltrarPorNomeEBusinessId() {
      UUID id = UUID.randomUUID();
      Category category = new Category();
      CategoryResponse response = new CategoryResponse(id, "Bebidas", "Refrigerante, Sucos, Aguas", Instant.now());
      Page<Category> page = new PageImpl<>(List.of(category), pageable, 1);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findAllByNameContainingIgnoreCaseAndBusinessId(
              "Bebidas", businessId, pageable)).thenReturn(page);
      when(mapper.toResponse(category)).thenReturn(response);

      Page<CategoryResponse> result = service.findAll("Bebidas", pageable);

      assertThat(result.getContent()).containsExactly(response);

      verify(categoryRepository).findAllByNameContainingIgnoreCaseAndBusinessId("Bebidas", businessId, pageable);
      verify(categoryRepository, never()).findAllByBusinessId(any(), any());
   }

   @Test
   void findAll_SemNome_DeveBuscarTodosPorBusinessId() {
      UUID id = UUID.randomUUID();
      Category category = new Category();
      CategoryResponse response = new CategoryResponse(id, "Bebidas", "Refrigerante, Sucos, Aguas", Instant.now());
      Page<Category> page = new PageImpl<>(List.of(category), pageable, 1);

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findAllByBusinessId(businessId, pageable)).thenReturn(page);
      when(mapper.toResponse(category)).thenReturn(response);

      Page<CategoryResponse> result = service.findAll(null, pageable);
      assertThat(result.getContent()).containsExactly(response);

      verify(categoryRepository).findAllByBusinessId(businessId, pageable);
      verify(categoryRepository, never()).findAllByNameContainingIgnoreCaseAndBusinessId(any(), any(), any());
   }

   @Test
   void findById_DeveBuscarPorIdEBusinessId() {
      UUID id = UUID.randomUUID();
      Category category = new Category();
      CategoryResponse response = new CategoryResponse(id, "Bebidas", "Refrigerante, Sucos, Aguas", Instant.now());

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.of(category));
      when(mapper.toResponse(category)).thenReturn(response);

      CategoryResponse result = service.findById(id);
      assertThat(result).isEqualTo(response);

      verify(categoryRepository).findByIdAndBusinessId(id, businessId);
   }

   @Test
   void findById_IdNaoEncontrado_DeveLancarNotFoundException() {
      UUID id = UUID.randomUUID();

      when(currentUser.getBusinessId()).thenReturn(businessId);
      when(categoryRepository.findByIdAndBusinessId(id, businessId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.findById(id))
              .isInstanceOf(NotFoundException.class)
              .hasMessage("Categoria não encontrada");

      verifyNoInteractions(mapper);
      verify(categoryRepository).findByIdAndBusinessId(id, businessId);
   }
}
