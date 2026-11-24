package com.ferreiradev.sistemamercado.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

   // Verifica se existe duplicidade de nome da categoria no mesmo business
   boolean existsByNameIgnoreCaseAndBusinessId(String name, UUID businessId);

   boolean existsByNameIgnoreCaseAndBusinessIdAndIdNot(String name, UUID businessId, UUID id);

   // Lista todas as categorias daquele business
   Page<Category> findAllByBusinessId(UUID businessId, Pageable pageable);

   Page<Category> findAllByNameContainingIgnoreCaseAndBusinessId(String name, UUID businessId, Pageable pageable);

   Optional<Category> findByNameAndBusinessId(String name, UUID businessId);

   Optional<Category> findByIdAndBusinessId(UUID id, UUID businessId);

   boolean deleteByIdAndBusinessId(UUID id, UUID businessId);



}
