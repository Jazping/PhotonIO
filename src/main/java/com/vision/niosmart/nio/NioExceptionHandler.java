package com.vision.niosmart.nio;

@FunctionalInterface
public interface NioExceptionHandler {
	void onThrow(Throwable throwable);
}
