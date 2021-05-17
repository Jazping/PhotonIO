package com.vision.niosmart.protocol;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.nio.NioBufferListener;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.GByteBuffer;

public class DefaultStreamProtocol<B> implements StreamProtocol{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
//	private Map<String,NioBufferListener> listenerMap;
	private ListenerProtocol listenerProtocol;
	public DefaultStreamProtocol() {
		super();
	}

	public DefaultStreamProtocol(ListenerProtocol listenerProtocol) {
		super();
		this.listenerProtocol = listenerProtocol;
	}

	@Override
	public boolean next(NioConnection s,GByteBuffer b) throws IOException {
		if(null==listenerProtocol) {
			logger.error("PROTOCOL NOT EXISTS");
			return true;
		}
		NioBufferListener bufferListener = null;
		try {
			bufferListener = listenerProtocol.getListener(b);
		}catch(IOException e) {
			return false;
		}catch(StreamException e) {
			return true;
		}
		try {
			return bufferListener==null?false:bufferListener.onBuffer(s,b);
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			return true;
		}
	}

	public ListenerProtocol getListenerProtocol() {
		return listenerProtocol;
	}

	public void setListenerProtocol(ListenerProtocol listenerProtocol) {
		this.listenerProtocol = listenerProtocol;
	}

}
