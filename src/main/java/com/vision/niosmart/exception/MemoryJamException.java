package com.vision.niosmart.exception;

public class MemoryJamException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4981622574992137762L;

	public MemoryJamException() {
		super();
	}

	public MemoryJamException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemoryJamException(String message) {
		super(message);
	}

	public MemoryJamException(Throwable cause) {
		super(cause);
	}

}
