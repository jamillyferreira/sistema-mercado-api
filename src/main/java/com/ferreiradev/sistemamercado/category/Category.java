package com.ferreiradev.sistemamercado.category;

import com.ferreiradev.sistemamercado.common.audit.BaseAudit;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
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

   @Column(length = 150)
   private String description;

   public Category(UUID businessId, String name, String description) {
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

   private boolean isNameValid(String name) {
      if (name == null || name.isEmpty()) {
         // TODO criar uma exception custom
         throw new IllegalArgumentException("Nome da categoria não pode ser vazia");
      }
      return true;
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
