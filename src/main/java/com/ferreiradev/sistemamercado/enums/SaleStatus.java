package com.ferreiradev.sistemamercado.enums;

public enum SaleStatus {
   ACTIVE("Ativa"),
   CANCELED("Cancelada"),;

   private final String description;

   SaleStatus(String description) {
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
