package com.vision.niosmart.protocol;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.nio.NioBufferListener;
import com.vision.niosmart.nio.buffer.GByteBuffer;

public interface ListenerProtocol {
	Logger LOGGER = LoggerFactory.getLogger(ListenerProtocol.class);
	NioBufferListener getListener(GByteBuffer b)throws IOException;
	int maxProtocolSize();
	
//	NioBufferListener getListener(GByteBuffer b,Map<String, NioBufferListener> map)throws IOException;
}
