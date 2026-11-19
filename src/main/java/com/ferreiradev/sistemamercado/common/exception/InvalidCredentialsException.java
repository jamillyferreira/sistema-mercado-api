package com.ferreiradev.sistemamercado.common.exception;

public class InvalidCredentialsException extends RuntimeException {
   public InvalidCredentialsException(String message) {
      super(message);
   }
}
