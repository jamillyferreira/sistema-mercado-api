package com.ferreiradev.sistemamercado.auth;

import com.ferreiradev.sistemamercado.auth.dto.LoginRequest;
import com.ferreiradev.sistemamercado.auth.dto.SignupRequest;
import com.ferreiradev.sistemamercado.auth.dto.AuthResponse;
import com.ferreiradev.sistemamercado.business.Business;
import com.ferreiradev.sistemamercado.business.BusinessRepository;
import com.ferreiradev.sistemamercado.common.exception.EmailAlreadyExistsException;
import com.ferreiradev.sistemamercado.common.exception.InvalidCredentialsException;
import com.ferreiradev.sistemamercado.config.security.JwtService;
import com.ferreiradev.sistemamercado.user.User;
import com.ferreiradev.sistemamercado.user.UserRepository;
import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;
import com.ferreiradev.sistemamercado.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
   private final UserRepository userRepository;
   private final BusinessRepository businessRepository;
   private final PasswordEncoder passwordEncoder;
   private final JwtService jwtService;
   private final UserMapper userMapper;

   // Signup de admin
   @Transactional
   public AuthResponse signup(SignupRequest request) {
      log.info("Tentativa de criar usuário admin");
      if (userRepository.existsByEmailIgnoreCase(request.email())) {
         log.warn("E-mail existente no sistema");
         throw new EmailAlreadyExistsException("Endereço de e-mail já está em uso");
      }

      Business business = Business.create(request.businessType()); // O create do Business já valida
      Business savedBusiness = businessRepository.save(business); // Salva o business e gera o UUID

      String hashedPassword = passwordEncoder.encode(request.password());

      User admin = User.createAdmin(
              request.name(),
              request.email(),
              hashedPassword,
              savedBusiness.getId()
      );

      User savedUser = userRepository.save(admin);

      String token = jwtService.generateToken(savedUser); // gera token a partir do user salvo
      long expiresIn = jwtService.expiresInSeconds();
      log.debug("Token JWT gerado: userId={}, expiresIn={}", savedUser.getId(), expiresIn);

      UserResponse userDto = userMapper.toResponse(savedUser);

      log.info("Signup bem sucedido: userId={}, role={}", savedUser.getId(), savedUser.getRole());
      return new AuthResponse(token, AuthResponse.BEARER, expiresIn, userDto);
   }

   // Login unico para ADMIN e EMPLOYEE
   // TODO: rate limiting no login
   @Transactional(readOnly = true) // somente ler dados, nao modifica nada
   public AuthResponse login(LoginRequest request) {
      log.info("Tentativa de login com email ou username: identifier={}", request.identifier());
      User user = findByEmailOrUsername(request.identifier());

      if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
         log.warn("Login falhou (senha incorreta): userId={}, identifier={}", user.getId(), request.identifier());
         throw new InvalidCredentialsException("Credenciais inválidas");
      }

      if (!user.isActive()) {
         log.warn("Login falhou (usuário desativado): userId={}, identifier={}", user.getId(), request.identifier());
         throw new InvalidCredentialsException("Credenciais inválidas");
      }

      String token = jwtService.generateToken(user);
      long expiresIn = jwtService.expiresInSeconds();
      UserResponse userDto = userMapper.toResponse(user);

      log.info("Login bem sucedido: userId={}, role={}", userDto.id(), userDto.role());
      return new AuthResponse(token, AuthResponse.BEARER, expiresIn, userDto);
   }

   private User findByEmailOrUsername(String identifier) {
      if (identifier.contains("@")) {
         return userRepository.findByEmailIgnoreCase(identifier)
                 .orElseThrow(() -> {
                    log.warn("Login falhou (e-mail não encontrado): identifier={}", identifier);
                    return new InvalidCredentialsException("Credenciais inválidas");
                 });
      }
      return userRepository.findByUsernameIgnoreCase(identifier)
              .orElseThrow(() -> {
                 log.warn("Login falhou (username não encontrado): identifier={}", identifier);
                 return new InvalidCredentialsException("Credenciais inválidas");
              });
   }
}
