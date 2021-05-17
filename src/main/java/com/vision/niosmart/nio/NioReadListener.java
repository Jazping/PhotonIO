package com.vision.niosmart.nio;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.IdempotentStreamProvider;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.util.NioBufferUtils;
import com.vision.niosmart.util.RequestBufferUtils;

public class NioReadListener<B> extends NioListener {
	private IdempotentStreamProvider provider;
	private BufferFactory<B> factory;
	public NioReadListener(DataBufferFactory dbf,BufferFactory<B> buffer, Context context,IdempotentStreamProvider provider) {
		super(dbf,context, true);
		this.factory = buffer;
		this.provider = provider;
	}
	
	@Override
	protected boolean onBuffer(long id, NioConnection session, GByteBuffer buffer) throws IOException {
		return this.headRequest(session.getId(),"READ",buffer, (line,headers)->{
			DataBuffer db = dbf.getDataBuffer();
			try {
				String[] array = line.trim().split(" ");
				String namestr = array[0];
				boolean udp = "UDP".equals(array[1]);
				if(isAcceptable(namestr,udp,session.getRemoteAddress())) {
					long connectionId = session.getId();
					Connection connection = context.getConnection(connectionId);
					int segmentSize = connection.getRemoteSegSize();
					IdempotentStream stream = provider.getIdempotentStream(namestr,headers);
					stream.setId(connection.genStramId());
					SegmentIdempotentStream datagram = putDatagramStream(udp,connectionId, stream, segmentSize);
					RequestBufferUtils.writeBuffer(datagram.getId(), datagram.getSegments(), namestr, stream.length(),null,udp,connectionId,db);
					NioBufferUtils.writeRetry(session, db, factory);
					session.flush();
				}
				return true;
			}catch(Throwable e) {
				throw new StreamException(e.getMessage(),e);
			}finally {
				db.release();
			}
		},false);
	}
	
	private SegmentIdempotentStream putDatagramStream(boolean udp, long connectionId, IdempotentStream stream, 
			int segmentSize) throws IOException {
		return udp?context.putDatagramStreamUDP(connectionId, stream.name(),stream, segmentSize):
			context.putDatagramStream(connectionId, stream.name(),stream, segmentSize);
	}
	
	/**
	 * if return true, that will be write data to remote host. false to reject this operation.
	 * @param stream
	 * @param remoteAddress
	 * @return
	 */
	protected boolean isAcceptable(String fileName,boolean isUdp,InetSocketAddress remoteAddress) {
		return true;
	}
}
