package com.ferreiradev.sistemamercado.enums;

public enum PaymentMethod {
   CASH("Dinheiro"),
   PIX("Pix"),
   DEBIT_CARD("Debito"),
   CREDIT_CARD("Credito"),;

   private final String description;

   PaymentMethod(String description) {
      this.description = description;
   }

   public String getDescription() {
      return this.description;
   }

   @Override
   public String toString() {
      return description;
   }
}
