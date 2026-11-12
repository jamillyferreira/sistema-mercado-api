package com.ferreiradev.sistemamercado.user;

import com.ferreiradev.sistemamercado.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
   boolean existsByEmailIgnoreCase(String email);

   boolean existsByUsernameIgnoreCase(String username);

   Optional<User> findByEmailIgnoreCase(String email);

   Optional<User> findByUsernameIgnoreCase(String username);

   boolean existsByRole(Role role);


}
