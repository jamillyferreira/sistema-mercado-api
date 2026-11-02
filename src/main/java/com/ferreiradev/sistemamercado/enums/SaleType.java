package com.ferreiradev.sistemamercado.enums;

public enum SaleType {
   IMMEDIATE("Avista"),
   STORE_CREDIT("Fiado");

   private final String description;

   SaleType(String description) {
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
