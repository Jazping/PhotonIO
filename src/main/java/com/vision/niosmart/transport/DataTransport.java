package com.vision.niosmart.transport;

import java.io.IOException;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.SegmentIdempotentStream;

public interface DataTransport<E extends Protocol> {
//	void transport(NioConnection session,SegmentIdempotentStream stream)throws IOException;
	int transport(NioConnection session,SegmentIdempotentStream stream)throws IOException;
	TransportConf geTransportConf();
	TransportIoHandler<E> getIoHandler();
	TransportListener getTransportListener();
	void close();
	
}
