package com.ferreiradev.sistemamercado.user;

import com.ferreiradev.sistemamercado.common.audit.BaseAudit;
import com.ferreiradev.sistemamercado.common.exception.BusinessException;
import com.ferreiradev.sistemamercado.common.exception.InvalidEmailException;
import com.ferreiradev.sistemamercado.user.enums.Role;
import com.ferreiradev.sistemamercado.common.exception.InvalidUsernameException;
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
   public static User createAdmin(String name, String email, String passwordHash, UUID businessId) {
      String validEmail = validateEmail(email);
      validatePasswordHash(passwordHash);

      User user = new User();
      user.name = name;
      user.email = validEmail;
      user.passwordHash = passwordHash; // sempre armazena HASH, nunca texto puro
      user.businessId = businessId;
      user.role = Role.ADMIN;
      user.active = true;
      return user;
   }

   // TODO: criar factory do employee
   public static User createEmployee(String name, String username, String passwordHash, UUID businessId) {
      String validUsername = validateUsername(username);
      validatePasswordHash(passwordHash);

      User user = new User();
      user.name = name;
      user.username = validUsername;
      user.passwordHash = passwordHash;
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


   private static String validateEmail(String email) {
      if (email == null || email.isBlank()) {
         throw new BusinessException("E-mail é obrigatório");
      }

      String normalizedEmail = email.trim().toLowerCase();
      if (!normalizedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
         throw new InvalidEmailException("E-mail inválido");
      }
      return normalizedEmail;
   }

   private static String validateUsername(String username) {
      if (username == null || username.isBlank()) {
         throw new BusinessException("Username é obrigatório");
      }
      if (!username.matches("^[a-zA-Z0-9._-]{3,50}$")) {
         throw new InvalidUsernameException("Username é inválido");
      }
      return username;
   }

   private static void validatePasswordHash(String passwordHash) {
      if (passwordHash == null || passwordHash.isBlank()) {
         throw new BusinessException("Senha é obrigatória");
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
