package com.ferreiradev.sistemamercado.enums;

public enum StockMovementType {
   STOCK_IN("Entrada"),
   SALE("Venda"),
   SALE_CANCELED("Cancelamento_venda"),
   ADJUSTMENT("Ajuste");

   private final String description;

   StockMovementType(final String description) {
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
