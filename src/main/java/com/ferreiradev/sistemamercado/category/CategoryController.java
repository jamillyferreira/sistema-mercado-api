package com.ferreiradev.sistemamercado.category;

import com.ferreiradev.sistemamercado.category.dto.CategoryDetailsResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryRequest;
import com.ferreiradev.sistemamercado.category.dto.CategoryResponse;
import com.ferreiradev.sistemamercado.category.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
   private final CategoryService categoryService;

   @PostMapping
   public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
      CategoryResponse response = categoryService.create(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

   @PatchMapping("/{id}")
   public ResponseEntity<CategoryDetailsResponse> update(
           @PathVariable UUID id, @RequestBody @Valid CategoryUpdateRequest request) {
      CategoryDetailsResponse response = categoryService.update(id, request);
      return ResponseEntity.status(HttpStatus.OK).body(response);

   }

   @GetMapping
   public ResponseEntity<Page<CategoryResponse>> findAll(
           @RequestParam(required = false) String name,
           @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
      Page<CategoryResponse> response = categoryService.findAll(name, pageable);
      return ResponseEntity.status(HttpStatus.OK).body(response);
   }

   @GetMapping("/{id}")
   public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id) {
      CategoryResponse response = categoryService.findById(id);
      return ResponseEntity.status(HttpStatus.OK).body(response);
   }

}
