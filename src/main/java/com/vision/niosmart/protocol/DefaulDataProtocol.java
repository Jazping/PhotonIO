package com.vision.niosmart.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.exception.ProtocolException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.transport.TransportIoHandler;
import com.vision.niosmart.transport.TransportListener;
import com.vision.niosmart.util.ThreadUtil;

public class DefaulDataProtocol implements DataProtocol<Meta>{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private TransportListener listener; 
	
	public DefaulDataProtocol(TransportListener listener) {
		this.listener = listener;
	}
	@Override
	public Meta dumpProtocol(long id,InetSocketAddress fromAddress, GByteBuffer buf) throws ProtocolException {
		Meta m = new Meta();
		m.setConnectionId(id);
		if(buf.remaining()<4) {
			throw new ProtocolException();
		}
		m.setStreamId(buf.readInt());
		if(buf.remaining()<4) {
			throw new ProtocolException();
		}
		m.setSegment(buf.readInt());
		if(buf.remaining()<8) {
			throw new ProtocolException();
		}
		long pos = buf.readLong();
		if(pos<0) {
			throw new ProtocolException("nagtive pos "+pos);
		}
		m.setPos(pos);
		if(buf.remaining()<2) {
			throw new ProtocolException();
		}
		short length = buf.readShort();
		if(buf.remaining()<length) {
			throw new ProtocolException();
		}
		m.setLength(length);
		return m;
	}

	@Override
	public boolean isAvailable(Context ctx, Meta e) {
		return ctx.isConnectionAvailable(e.getConnectionId());
	}

	@Override
	public void beforeRecvice(TransportIoHandler<Meta> h,NioConnection s,Context ctx,Meta e) throws IOException {
		h.onReceive(e.getConnectionId(), e.getStreamId(), e.getSegment());
	}

	@Override
	public boolean recvice(Context ctx, Meta e,byte[] packet,int offset,int length) {
		return ctx.fillStream(e.getConnectionId(), e.getStreamId(), e.getSegment(), e.getPos(),packet,0,length);
	}

	@Override
	public void onDataRepeat(InetSocketAddress fromAddress,Context ctx, Meta e) {
		if(listener!=null) {
			listener.onRepeat(fromAddress, e.getConnectionId(), e.getStreamId(), e.getSegment());
		}
	}

	@Override
	public boolean isFully(Context ctx, Meta e) {
		return ctx.isStreamFully(e.getConnectionId(), e.getStreamId());
	}

	@Override
	public boolean finish(Context ctx, Meta e) throws IOException {
		return ctx.finishStream(e.getConnectionId(), e.getStreamId());
	}

	@Override
	public Stream cleanUp(Context ctx, Meta e) {
		Stream stream = null;
		Connection conn = ctx.getConnection(e.getConnectionId());
		if(conn!=null) {
			stream = conn.removeStream(e.getStreamId());
			ctx.removeGetting(stream.getName());
			ThreadUtil.notify(conn);
			logger.debug("Connection {} stream {} all segments received",e.getConnectionId(),e.getStreamId());
		}
		return stream;
	}

	@Override
	public void transfer(Stream stream) {
		if(listener!=null) {
			listener.onFinished(stream);
		}
	}
	@Override
	public void buildDataBuffer(GByteBuffer dBuffer, long connectionId, int streamId, int segment, long pos)
			throws IOException {
		dBuffer.writeString("DATA");
		dBuffer.writeInt(streamId);
		dBuffer.writeInt(segment);
		dBuffer.writeLong(pos);
	}
	
}
