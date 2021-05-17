package com.vision.niosmart.exception;

public class BufferException extends Http3Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 289158357113969912L;

	public BufferException() {
		super();
	}

	public BufferException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BufferException(String message, Throwable cause) {
		super(message, cause);
	}

	public BufferException(String message) {
		super(message);
	}

	public BufferException(Throwable cause) {
		super(cause);
	}

}
