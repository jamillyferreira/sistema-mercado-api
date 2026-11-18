package com.ferreiradev.sistemamercado.auth;

import com.ferreiradev.sistemamercado.auth.dto.SignupRequest;
import com.ferreiradev.sistemamercado.auth.dto.AuthResponse;
import com.ferreiradev.sistemamercado.business.Business;
import com.ferreiradev.sistemamercado.business.BusinessRepository;
import com.ferreiradev.sistemamercado.common.exception.EmailAlreadyExistsException;
import com.ferreiradev.sistemamercado.config.security.JwtService;
import com.ferreiradev.sistemamercado.user.User;
import com.ferreiradev.sistemamercado.user.UserRepository;
import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;
import com.ferreiradev.sistemamercado.user.enums.Role;
import com.ferreiradev.sistemamercado.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

   @Mock
   private UserRepository userRepository;

   @Mock
   private BusinessRepository businessRepository;

   @Mock
   private PasswordEncoder passwordEncoder;

   @Mock
   private UserMapper userMapper;

   @Mock
   private JwtService jwtService;

   @InjectMocks
   private AuthService authService;

   @Test
   void signup_deveCriarBusinessAdminEGerarToken() {
      UUID businessId = UUID.randomUUID();
      var request = new SignupRequest("Carol Brito", "carolbrito@example.com", "teste123", "Mercado");

      Business savedBusiness = mock(Business.class);
      when(savedBusiness.getId()).thenReturn(businessId);
      UserResponse userDto = mock(UserResponse.class);

      when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
      when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);
      when(passwordEncoder.encode(request.password())).thenReturn("hash-bcrypt");
      when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
      when(jwtService.generateToken(any(User.class))).thenReturn("token-jwt");
      when(jwtService.expiresInSeconds()).thenReturn(900L);
      when(userMapper.toResponse(any(User.class))).thenReturn(userDto);

      AuthResponse result = authService.signup(request);

      assertThat(result).isNotNull();
      assertThat(result.accessToken()).isEqualTo("token-jwt");
      assertThat(result.tokenType()).isEqualTo(AuthResponse.BEARER);
      assertThat(result.expiresIn()).isEqualTo(900L);
      assertThat(result.user()).isSameAs(userDto);

      ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      InOrder order = inOrder(businessRepository, userRepository);
      order.verify(businessRepository).save(businessCaptor.capture());
      order.verify(userRepository).save(userCaptor.capture());

      assertThat(businessCaptor.getValue().getBusinessType()).isEqualTo("Mercado");
      User savedUser = userCaptor.getValue();

      assertThat(savedUser.getName()).isEqualTo(request.name());
      assertThat(savedUser.getEmail()).isEqualTo(request.email());
      assertThat(savedUser.getPasswordHash()).isEqualTo("hash-bcrypt");
      assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
      assertThat(savedUser.isActive()).isTrue();
      assertThat(savedUser.getUsername()).isNull();
      assertThat(savedUser.getBusinessId()).isEqualTo(businessId);
   }

   @Test
   void signup_quandoEmailJaExiste_DeveLancarExceaoENaoGravarNada() {
      var request = new SignupRequest("Carol Brito", "carolbrito@example.com", "teste123", "Mercado");
      when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

      assertThatThrownBy(() -> authService.signup(request))
              .isInstanceOf(EmailAlreadyExistsException.class)
              .hasMessage("Endereço de e-mail já está em uso");

      verifyNoInteractions(businessRepository, userMapper, passwordEncoder, jwtService);
      verify(userRepository, never()).save(any());
   }
}