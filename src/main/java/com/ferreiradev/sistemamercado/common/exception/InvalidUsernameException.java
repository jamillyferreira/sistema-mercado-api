package com.ferreiradev.sistemamercado.common.exception;

public class InvalidUsernameException extends RuntimeException {
   public InvalidUsernameException(String message) {
      super(message);
   }
}
