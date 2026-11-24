package com.ferreiradev.sistemamercado.config.security;

import com.ferreiradev.sistemamercado.common.exception.UnauthorizedException;
import com.ferreiradev.sistemamercado.user.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUser {

   public UUID getId() {
      return UUID.fromString(getJwt().getSubject());
   }

   public String getName() {
      return getJwt().getClaimAsString("name");
   }

   public UUID getBusinessId() {
      return UUID.fromString(getJwt().getClaimAsString("businessId"));
   }

   public Role getRole() {
      return Role.valueOf(getJwt().getClaimAsString("role"));
   }

   public boolean isAdmin() {
      return getRole() == Role.ADMIN;
   }

   private Jwt getJwt() {
      return findJwt().orElseThrow(() -> new UnauthorizedException("Unauthorized"));
   }

   private Optional<Jwt> findJwt() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
         return Optional.empty();
      }
      return Optional.of(jwt);
   }
}
