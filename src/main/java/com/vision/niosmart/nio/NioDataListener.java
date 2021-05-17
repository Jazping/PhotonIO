package com.vision.niosmart.nio;

import java.io.IOException;

import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.transport.DataTransport;

public class NioDataListener implements NioBufferListener {
	private DataTransport<?> tcpt;
	
	public NioDataListener(DataTransport<?> tcpt) {
		super();
		this.tcpt = tcpt;
	}

	@Override
	public boolean onBuffer(NioConnection session,GByteBuffer buffer) throws IOException {
		return tcpt.getIoHandler().doReceive(session,buffer,session.getId());
	}

}
