package com.ferreiradev.sistemamercado.business;

import com.ferreiradev.sistemamercado.common.audit.BaseAudit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "business")
public class Business extends BaseAudit {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;

   @Column(length = 150)
   private String name;

   @Column(name = "business_type", nullable = false, length = 100)
   private String businessType;

   @Size(max = 100)
   private String city;

   @Size(max = 2)
   private String state;

   @Size(max = 255)
   private String address;

   @Size(max = 20)
   private String phone;

   @Size(max = 14)
   private String cnpj;

   @Column(nullable = false)
   private boolean active = true;

   public static Business create(String businessType) {
      validateBusinessType(businessType);
      Business newBusiness = new Business();
      newBusiness.businessType = businessType.trim();
      newBusiness.active = true;
      return newBusiness;
   }

   public void updateFullField(String name, String businessType, String city, String state,
                               String address, String phone, String cnpj) {
      this.name = name;
      this.businessType = businessType;
      this.city = city;
      this.state = state;
      this.address = address;
      this.phone = phone;
      this.cnpj = cnpj;
   }

   private static void validateBusinessType(String businessType) {
      if (businessType == null || businessType.isBlank()) {
         throw new IllegalArgumentException("Tipo de negócio é obrigatório");
      }
   }

   @Override
   public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      Business business = (Business) o;
      return Objects.equals(id, business.id);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(id);
   }
}
