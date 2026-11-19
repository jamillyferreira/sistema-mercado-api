package com.ferreiradev.sistemamercado.config.security;

import com.ferreiradev.sistemamercado.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {
   @Value("${jwt.expiration-minutes}")
   private long expirationMinutes;

   @Value("${jwt.issuer}")
   private String issuer;

   private final JwtEncoder jwtEncoder;

   public JwtService(JwtEncoder jwtEncoder) {
      this.jwtEncoder = jwtEncoder;
   }

   public String generateToken(User user) {
      Instant now = Instant.now();

      JwtClaimsSet claims = JwtClaimsSet.builder()
              .issuer(issuer)
              .subject(user.getId().toString())
              .claim("businessId", user.getBusinessId().toString())
              .claim("name", user.getName())
              .claim("role", user.getRole().name())
              .issuedAt(now)
              .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
              .build();

      // jws significa que o token é assinado, JwsHeader é uma class do spring que representa o header do jwt
      JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
      // aplica codificacao passando o header e o calims
      return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
   }

   public long expiresInSeconds() {
      return expirationMinutes * 60;
   }
}
