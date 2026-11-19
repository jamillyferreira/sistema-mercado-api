package com.ferreiradev.sistemamercado.config.security;

import com.ferreiradev.sistemamercado.common.exception.UnauthorizedException;
import com.ferreiradev.sistemamercado.user.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

   public UUID id() {
      return UUID.fromString(getJwt().getSubject());
   }

   public String getName() {
      return getJwt().getClaimAsString("name");
   }

   public UUID businessId() {
      return UUID.fromString(getJwt().getClaimAsString("businessId"));
   }

   public Role role() {
      return Role.valueOf(getJwt().getClaimAsString("role"));
   }

   public boolean isAdmin() {
      return role() == Role.ADMIN;
   }

   private Jwt getJwt() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
         throw new UnauthorizedException("Nenhum usuário autenticado");
      }
      return jwt;
   }
}
