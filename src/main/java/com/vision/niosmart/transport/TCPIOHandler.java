package com.vision.niosmart.transport;

import java.io.IOException;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;

public class TCPIOHandler<E extends Protocol> extends TransportIoHandler<E> {

	public TCPIOHandler(Context context,DataBufferFactory df,DataProtocol<E> dp) {
		super(context,df,dp);
	}

	@Override
	protected void confirm(GByteBuffer buf) throws IOException {

	}

	@Override
	public void onReceive(long connectionId, int streamId, int segment) throws IOException {
		
	}

//	@Override
//	protected void flushConirm(InetSocketAddress destination) {
//		
//	}

}
