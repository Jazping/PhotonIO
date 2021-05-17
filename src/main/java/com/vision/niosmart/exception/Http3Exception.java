package com.vision.niosmart.exception;

public class Http3Exception extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1290952469642452801L;

	public Http3Exception() {
		super();
	}

	public Http3Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Http3Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public Http3Exception(String message) {
		super(message);
	}

	public Http3Exception(Throwable cause) {
		super(cause);
	}

}
