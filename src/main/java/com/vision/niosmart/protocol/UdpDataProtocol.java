package com.vision.niosmart.protocol;

import java.io.IOException;

import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.transport.TransportListener;

public class UdpDataProtocol extends DefaulDataProtocol{

	public UdpDataProtocol(TransportListener listener) {
		super(listener);
	}
	
//	@Override
//	public Meta dumpProtocol(long id,InetSocketAddress fromAddress, GByteBuffer buf) throws ProtocolException {
//		if(buf.remaining()<8) {
//			throw new ProtocolException();
//		}
//		return super.dumpProtocol(id, fromAddress, buf);
//	}
	
	@Override
	public void buildDataBuffer(GByteBuffer dBuffer, long connectionId, int streamId, int segment, long pos)
			throws IOException {
		dBuffer.writeShort(1);
		dBuffer.writeLong(connectionId);
		dBuffer.writeInt(streamId);
		dBuffer.writeInt(segment);
		dBuffer.writeLong(pos);
	}
	
}
