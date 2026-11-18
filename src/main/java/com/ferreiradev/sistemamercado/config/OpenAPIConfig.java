package com.ferreiradev.sistemamercado.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

   @Bean
   public OpenAPI customOpenAPI() {
      Contact contact = new Contact()
              .name("Jamilly Ferreira")
              .url("https://github.com/jamillyferreira")
              .email("jamillyferreira.dev@gmail.com");

      Info info = new Info()
              .title("Sistema de Mercado")
              .description("API responsável pelo gerenciamento de Estoque, Funcionarios e Compras fiados")
              .version("1.0")
              .contact(contact);

      Server localServer = new Server()
              .url("http://localhost:8080")
              .description("Servidor de desenvolvimento");

      return new OpenAPI()
              .info(info)
              .addServersItem(localServer);
   }
}
