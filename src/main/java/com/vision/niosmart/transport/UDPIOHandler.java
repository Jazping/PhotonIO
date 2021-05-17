package com.vision.niosmart.transport;

import java.io.IOException;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.SegmentIdempotentStream;

public abstract class UDPIOHandler<E extends Protocol> extends TransportIoHandler<E>{
	private static byte[] yes = new byte[] {1};
	private UDPDataTransport<E> transport;
	public UDPIOHandler(UDPDataTransport<E> transport, Context context, DataBufferFactory df,DataProtocol<E> dp) {
		super(context,df,dp);
		this.transport = transport;
	}
	
//	@Override
//	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//		InetSocketAddress address = (InetSocketAddress) session.getRemoteAddress();
//		getTransport().removeCache(address);
//		logger.warn("socket exception,removed this connection {}-{}",session,cause);
//		session.closeNow();
//	}
	
	protected void confirm(GByteBuffer buf) throws IOException {
		long connectionId = buf.readLong();
		int stream = buf.readInt();
		int segment = buf.readInt();
		int len = buf.readShort();
		byte[] data = new byte[len];
		buf.read(data);
		SegmentIdempotentStream datagram = context.getDatagramStream(connectionId, stream);
		if(datagram==null) {
			return;
		}
		TransportMonitor monitor = datagram.getTransportMonitor();
		if(datagram.set(segment)&&monitor!=null) {
			monitor.confirm();
		}
	}
	
	public void onReceive(long connectionId, int streamId, int segment) throws IOException {
		Connection conn = context.getConnection(connectionId);
		if(conn!=null) {
			NioConnection session = conn.getUdpConnection(false);
			connectionId = conn.getClientId()==null?connectionId:conn.getClientId();
			getTransport().send(0, connectionId, streamId, segment, yes,0,yes.length, session);
			session.flush();
		}
	}
	
	protected UDPDataTransport<E> getTransport() {
		return this.transport;
	}
}
