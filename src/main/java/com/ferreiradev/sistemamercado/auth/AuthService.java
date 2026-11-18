package com.ferreiradev.sistemamercado.auth;

import com.ferreiradev.sistemamercado.auth.dto.SignupRequest;
import com.ferreiradev.sistemamercado.auth.dto.SignupResponse;
import com.ferreiradev.sistemamercado.business.Business;
import com.ferreiradev.sistemamercado.business.BusinessRepository;
import com.ferreiradev.sistemamercado.common.exception.EmailAlreadyExistsException;
import com.ferreiradev.sistemamercado.config.security.JwtService;
import com.ferreiradev.sistemamercado.user.User;
import com.ferreiradev.sistemamercado.user.UserRepository;
import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;
import com.ferreiradev.sistemamercado.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
   public SignupResponse signup(SignupRequest request) {
      if (userRepository.existsByEmailIgnoreCase(request.email())) {
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
      UserResponse userDto = userMapper.toResponse(savedUser);

      return new SignupResponse(token, SignupResponse.BEARER, expiresIn, userDto);
   }

   // TODO: Criar login do admin, aceita email ou username para logar (username precisa existir)
   // TODO: Criar login do employee, só pode logar com username
}
