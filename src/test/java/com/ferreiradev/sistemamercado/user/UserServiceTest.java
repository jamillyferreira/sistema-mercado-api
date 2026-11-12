package com.ferreiradev.sistemamercado.user;

import com.ferreiradev.sistemamercado.business.Business;
import com.ferreiradev.sistemamercado.business.BusinessRepository;
import com.ferreiradev.sistemamercado.user.dto.request.SignupRequest;
import com.ferreiradev.sistemamercado.user.dto.response.UserResponse;
import com.ferreiradev.sistemamercado.user.enums.Role;
import com.ferreiradev.sistemamercado.user.exception.EmailAlreadyExistsException;
import com.ferreiradev.sistemamercado.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

   @Mock
   private UserRepository userRepository;

   @Mock
   private UserMapper userMapper;

   @Mock
   private BusinessRepository businessRepository;

   @InjectMocks
   private UserService userService;

   @Test
   @DisplayName("Deve criar business e admin com sucesso")
   void createUserAdminWithSuccess() {

      // Cria um id do business
      UUID businessId = UUID.randomUUID();
      Business savedBusiness = Business.create("Perfumaria");
      ReflectionTestUtils.setField(savedBusiness, "id", businessId);

      var request = new SignupRequest("Caroline Brito", "carolbrito@example.com", "br12345", "Perfumaria");
      var expected = UserResponse.builder()
              .id(UUID.randomUUID())
              .name("Caroline Brito")
              .email("carolbrito@example.com")
              .username(null)
              .phone(null)
              .role(Role.ADMIN)
              .active(true)
              .businessId(businessId)
              .build();

      when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);
      when(userRepository.existsByEmailIgnoreCase("carolbrito@example.com")).thenReturn(false);

      when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
      when(userMapper.toResponse(any(User.class))).thenReturn(expected);

      UserResponse result = userService.signup(request);
      assertThat(result).isEqualTo(expected);

      ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

      // Verifica a ordem que foi chamado, InOrder ja faz o verify (nao precisa ter os dois)
      InOrder inOrder = inOrder(businessRepository, userRepository);
      inOrder.verify(businessRepository).save(businessCaptor.capture()); // Primeiro
      inOrder.verify(userRepository).save(userCaptor.capture()); // Segundo

      assertThat(businessCaptor.getValue().getBusinessType()).isEqualTo("Perfumaria");

      User savedUser = userCaptor.getValue();
      assertThat(savedUser.getName()).isEqualTo(request.name());
      assertThat(savedUser.getEmail()).isEqualTo(request.email());
      assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
      assertThat(savedUser.isActive()).isTrue();
      assertThat(savedUser.getUsername()).isNull();
      assertThat(savedUser.getBusinessId()).isEqualTo(businessId);
   }

   @Test
   @DisplayName("Deve lançar excecao se email ja existir")
   void createAdmin_WhenEmailExists_ThrowAndNotSave() {
      var request = new SignupRequest("Caroline Brito", "carolbrito@example.com", "br12345", "Perfumaria");

      // Simula que email existe, ou seja, vai lancar a exception
      when(userRepository.existsByEmailIgnoreCase("carolbrito@example.com")).thenReturn(true);

      assertThatThrownBy(() -> userService.signup(request))
              .isInstanceOf(EmailAlreadyExistsException.class);

      verify(businessRepository, never()).save(any());
      verify(userRepository, never()).save(any());
   }

}