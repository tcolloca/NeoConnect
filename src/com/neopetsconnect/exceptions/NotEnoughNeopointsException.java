package com.neopetsconnect.exceptions;

public class NotEnoughNeopointsException extends Exception {

  private static final long serialVersionUID = -4327732114414296036L;

  public NotEnoughNeopointsException(String message) {
    super(message);
  }
}
