package com.ferreiradev.sistemamercado.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

   // Verifica se existe duplicidade de nome da categoria no mesmo business
   boolean existsByBusinessIdAndNameIgnoreCase(UUID businessId, String name);

   // Lista todas as categorias daquele business
   Page<Category> findAllByBusinessId(UUID businessId, Pageable pageable);
}
