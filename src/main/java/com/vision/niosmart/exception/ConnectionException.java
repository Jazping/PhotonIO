package com.vision.niosmart.exception;

public class ConnectionException extends Http3Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4120150811299939285L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

}
