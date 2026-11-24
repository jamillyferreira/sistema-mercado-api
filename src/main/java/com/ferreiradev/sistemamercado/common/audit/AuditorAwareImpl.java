package com.ferreiradev.sistemamercado.common.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

   @Override
   public Optional<UUID> getCurrentAuditor() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
         return Optional.empty();
      }
      return Optional.of(UUID.fromString(jwt.getSubject()));
   }

}
