package com.vision.niosmart.nio;

import com.vision.niosmart.exception.Http3Exception;

public class NioException extends Http3Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4684106815872336904L;

	public NioException() {
		super();
	}

	public NioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NioException(String message, Throwable cause) {
		super(message, cause);
	}

	public NioException(String message) {
		super(message);
	}

	public NioException(Throwable cause) {
		super(cause);
	}

}
