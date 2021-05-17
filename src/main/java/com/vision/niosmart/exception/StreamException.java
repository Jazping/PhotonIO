package com.vision.niosmart.exception;

public class StreamException extends Http3Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4488072423546387906L;

	public StreamException() {
		super();
	}

	public StreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public StreamException(String message) {
		super(message);
	}

	public StreamException(Throwable cause) {
		super(cause);
	}

}
