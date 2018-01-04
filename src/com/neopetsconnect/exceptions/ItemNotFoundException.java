package com.neopetsconnect.exceptions;

public class ItemNotFoundException extends Exception {

	private static final long serialVersionUID = -4327732114414296032L;

	public ItemNotFoundException(String message) {
		super(message);
	}
}
