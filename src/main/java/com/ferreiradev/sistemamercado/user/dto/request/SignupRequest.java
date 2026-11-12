package com.ferreiradev.sistemamercado.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Nome completo é obrigatório")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 255, message = "Senha precisa ter pelo menos 6 caracteres")
        String password,

        @NotBlank(message = "Tipo de negócio é obrigatório")
        @Size(max = 100, message = "Tipo de negócio muito longo")
        String businessType
        ) {

}
