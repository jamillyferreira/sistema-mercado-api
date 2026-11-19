package com.ferreiradev.sistemamercado.auth;

import com.ferreiradev.sistemamercado.auth.dto.LoginRequest;
import com.ferreiradev.sistemamercado.auth.dto.SignupRequest;
import com.ferreiradev.sistemamercado.auth.dto.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
   private final AuthService authService;

   @PostMapping("/signup")
   public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignupRequest request) {
      AuthResponse response = authService.signup(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

   @PostMapping("/login")
   public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
      AuthResponse response = authService.login(request);
      return ResponseEntity.status(HttpStatus.OK).body(response);
   }

}
