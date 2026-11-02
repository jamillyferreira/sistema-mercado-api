package com.ferreiradev.sistemamercado.category;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
public class Category {
   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;

   @Column(nullable = false, length = 150, unique = true)
   private String name;

   @Column(length = 150)
   private String description;

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
