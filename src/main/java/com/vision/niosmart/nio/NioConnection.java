package com.vision.niosmart.nio;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public interface NioConnection {
	String REV_BUFFER = "REV-BUFFER";
	String UPORT = "UPORT";
	String CLIENTID = "CLIENTID";
	String MAX_SEGMENT_SIZE = "MAX-SEGMENT-SIZE";
	String REMOTE_HOST = "REMOTE-HOST";
	String DEFAULT_PUBLIC_KEY = "DEFAULT-PUBLIC-KEY";
	String ENCRYPT_ATTESTATION = "ENCRYPT-ATTESTATION";
	void close();
	long getId();
	boolean isClosed();
	void write(Object message);
	void synWrite(Object message);
	void flush();
	Object getNative();
	boolean isEstablished();
	void establish();
	<T> T getAttribute(String key,T def);
	<T> T getAttribute(String key,Supplier<T> def);
	void setAttribute(String key,Object value);
	void removeAttribute(String key);
	boolean hasAttribute(String key);
	InetSocketAddress getRemoteAddress();
	void setExceptionHanlder(NioExceptionHandler handle);
}
