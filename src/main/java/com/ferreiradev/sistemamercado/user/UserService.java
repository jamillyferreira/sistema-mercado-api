package com.ferreiradev.sistemamercado.user;

import com.ferreiradev.sistemamercado.business.Business;
import com.ferreiradev.sistemamercado.business.BusinessRepository;
import com.ferreiradev.sistemamercado.user.dto.request.SignupRequest;
import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;
import com.ferreiradev.sistemamercado.user.exception.EmailAlreadyExistsException;
import com.ferreiradev.sistemamercado.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
   private final UserRepository userRepository;
   private final BusinessRepository businessRepository;
   private final UserMapper userMapper;

   // Cria tudo junto ou faz rollback se acontencer algum erro
   @Transactional
   public UserResponse signup(SignupRequest request) {
      if (userRepository.existsByEmailIgnoreCase(request.email())) {
         throw new EmailAlreadyExistsException("Endereço de e-mail já está em uso");
      }

      // TODO criar o business primeiro
      Business business = Business.create(request.businessType()); // O create do Business já valida
      Business savedBusiness = businessRepository.save(business); // Salva o business e gera o UUID

      // TODO criar o user admin depois que tiver o business
      User admin = User.createAdmin(
              request.name(),
              request.email(),
              request.password(), // TODO: encodar senha
              savedBusiness.getId()
      );

      User savedUser = userRepository.save(admin);
      return userMapper.toResponse(savedUser);
   }

   // TODO: criar employee (funcionario)


}
