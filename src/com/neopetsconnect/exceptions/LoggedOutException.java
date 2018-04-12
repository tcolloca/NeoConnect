package com.neopetsconnect.exceptions;

public class LoggedOutException extends RuntimeException {

  private static final long serialVersionUID = 5318839493344840395L;

  public LoggedOutException(String message) {
    super(message);
  }
}
