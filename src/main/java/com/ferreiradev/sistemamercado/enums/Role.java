package com.ferreiradev.sistemamercado.enums;

public enum Role {
   ADMIN("Proprietario"),
   EMPLOYEE("Funcionario"),;

   private final String description;

   Role(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   @Override
   public String toString() {
      return description;
   }
}
