package com.ferreiradev.sistemamercado.user;

import com.ferreiradev.sistemamercado.common.audit.BaseAudit;
import com.ferreiradev.sistemamercado.user.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
public class User extends BaseAudit {
   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;

   @Column(name = "business_id", nullable = false, updatable = false)
   private UUID businessId;

   @NotBlank
   @Column(nullable = false, length = 150)
   private String name;

   @Column(length = 50)
   private String username;

   @Email
   @Column(length = 150)
   private String email;

   @NotBlank
   @Column(name = "password_hash", nullable = false)
   private String passwordHash;

   @Column(length = 20)
   private String phone;

   @NotNull
   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private Role role;

   @Column(nullable = false)
   private boolean active;

   @Version
   @Column(nullable = false)
   private Long version;

   // Construtor privado
   private User() {
   }

   // Um factory para criar um admin
   public static User createAdmin(String name, String email, String password, UUID businessId) {
      User user = new User();
      user.name = name;
      user.email = email.trim().toLowerCase();
      user.passwordHash = password; // TODO: trocar por hash
      user.businessId = businessId;
      user.role = Role.ADMIN;
      user.active = true;
      return user;
   }

   // TODO: criar factory do employee
   public static User createEmployee(String name, String username, String password, UUID businessId) {
      validateUsername(username);
      User user = new User();
      user.name = name;
      user.username = username.trim().toLowerCase();
      user.passwordHash = password; // TODO: trocar por hash
      user.role = Role.EMPLOYEE;
      user.businessId = businessId;
      user.active = true;
      return user;
   }

   public void deactivate() {
      this.active = false;
   }

   public void activate() {
      this.active = true;
   }

   private static void validateUsername(String username) {
      if (username == null || !username.matches("^[a-zA-Z0-9._-]{3,50}$")) {
         throw new IllegalArgumentException("Username é inválido"); // TODO: Criar exception custom
      }
   }

   @Override

   public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      User user = (User) o;
      return Objects.equals(id, user.id);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(id);
   }
}
