package com.ferreiradev.sistemamercado.common.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

   @Override
   public Optional<UUID> getCurrentAuditor() {
      // TODO: quando houver login, ler do SecurityContext
      // Enquanto isso o createdBy/updatedBy ficam null (bootstrap)
      return Optional.empty();
   }
}
