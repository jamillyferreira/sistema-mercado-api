package com.ferreiradev.sistemamercado.category;

import com.ferreiradev.sistemamercado.common.audit.BaseAudit;
import com.ferreiradev.sistemamercado.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "categories")
public class Category extends BaseAudit {
   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;

   @Column(name = "business_id", nullable = false)
   private UUID businessId;

   @Column(nullable = false, length = 150)
   private String name;

   @Column(length = 255)
   private String description;

   public Category(UUID businessId, String name, String description) {
      isNameValid(name);
      this.businessId = businessId;
      this.name = name;
      this.description = description;
   }

   // metodo de atualizar campos da categoria sem precisar chamar setter individualmente
   public void updateCategory(String name, String description) {
      isNameValid(name);
      this.name = name;
      this.description = description; // description é um campo opcional
   }

   private void isNameValid(String name) {
      if (name == null || name.isEmpty()) {
         throw new BusinessException("Nome da categoria não pode ser vazia");
      }
   }

   @Override
   public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      Category category = (Category) o;
      return Objects.equals(id, category.id);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(id);
   }
}
