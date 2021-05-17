package com.vision.niosmart.transport;

import java.io.IOException;
import java.util.concurrent.Executor;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;

public class TCPDataTransport<B,E extends Protocol> extends AbstractDataTransport<B,E> implements DataTransport<E> {
	public TCPDataTransport(Executor executor,Context context, BufferFactory<B> gbBuffer,
			DataBufferFactory dbf,DataProtocol<E> dp) throws IOException {
		this(executor, context, new DefaultTransportConf(READ_BUFFE_SIZE,
				PROTOCOL_LENGTH,REC_BUFFE_SIZE,
				REC_BUFFE_SIZE),
				gbBuffer,dbf,dp);
	}
	
	public TCPDataTransport(Executor executor,Context context,TransportConf conf, BufferFactory<B> gbBuffer,
			DataBufferFactory dbf,DataProtocol<E> dp) {
		super(executor,context,conf,gbBuffer,dbf);
		super.setIoHandler(new TCPIOHandler<>(context,dbf,dp));
	}

	@Override
	public void close() {
		
	}
}
