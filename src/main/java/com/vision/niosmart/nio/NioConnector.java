package com.vision.niosmart.nio;

public interface NioConnector {
	NioConnection connect();
	long currentId();
	void disponse();
	boolean isDisposed();
}
