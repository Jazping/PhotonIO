package com.vision.niosmart.transport;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.SegmentIdempotentStream;

public abstract class AbstractDataTransport<B,E extends Protocol> implements DataTransport<E> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	public static final int REC_BUFFE_SIZE = 1024*1024*4;
	public static final int READ_BUFFE_SIZE = 4096;
	public static final int BIG_BUFFER_SIZE = 1024*1024*4;
	public static final int PROTOCOL_LENGTH = 24+11;
//	public static final int BATCH = 10000;
	public static final int WAIT_EVERY_BATCH = 1000;
	private TransportIoHandler<E> handler;
	protected Context context;
	protected Executor executor;
	protected BufferFactory<B> gBuffer;
	protected DataBufferFactory dbf;
	protected DataProtocol<?> protocol;
	protected TransportConf conf;
	
	protected AbstractDataTransport(Executor executor,Context context,TransportConf conf,BufferFactory<B> gBuffer,
			DataBufferFactory dbf) {
		this.executor = executor;
		this.context = context;
		this.conf = conf;
		this.gBuffer = gBuffer;
		this.dbf = dbf;
	}
	
	@Override
	public int transport(NioConnection session, SegmentIdempotentStream stream) throws IOException {
		int batch = 40;
		int start = stream.getSegments()-stream.remaining();
		int segment = batchSend(stream,start,batch,session);
		stream.set(start,segment);
		return segment==-1?-1:batch;
	}
	
//	@Override
//	public void transport(NioConnection session, SegmentIdempotentStream stream) throws IOException {
//		if(!stream.isClosed()) {
//			synchronized (stream) {
//				if(stream.getConnectionId()!=session.getId()) {
//					logger.error("security exception, connection {} stream {}",session.getId(),stream.getId());
//					return;
//				}
//				Connection con = context.getConnection(session.getId());
//				if(con==null) {
//					logger.error("transport interrupted, connection {} stream {}",session.getId(),stream.getId());
//					return;
//				}
//				int batch = 40;
//				int counter = 0;
//				int start = stream.getSegments()-stream.remaining();
//				int segment = batchSend(stream,start,batch,session);
//				while(counter<3&&!stream.isClosed()&&stream.remaining()>0) {
//					int s = batchSend(stream,segment,batch,session);
//					if(s==segment) {
//						counter++;
//					}else {
//						counter=0;
//						segment = s;
//					}
//				}
//				if(counter==3) {
//					logger.warn("transport give up connection {} stream length {}",stream.getConnectionId(),stream.length());
//				}else {
//					logger.info("transport connection {} stream length {} finished",stream.getConnectionId(),stream.length());
//				}
//				session.flush();
//			}
//		}
//	}
	
	protected int batchSend(SegmentIdempotentStream stream,int start,int batch,NioConnection session) throws IOException  {
		int segments = stream.getSegments();
		long connectionId = stream.getConnectionId();
		int segmentPos = -1;
		for(int i=start;i<segments&&batch>0;i++) {
			if(!stream.get(i)&&!stream.isClosed()&&!session.isClosed()) {
				batch--;
				int length = stream.getSegmentLength(i);
				long pos = stream.getPosition(i);
				int segment = stream.getSegment(i);
				write(length,connectionId,stream,pos,segment,session);
				markSegment(stream,i);
				segmentPos = i;
			}
		}
		return segmentPos;
	}
	
	private void write(int length,long connectionId,SegmentIdempotentStream stream,
			long pos,int segment,NioConnection session) throws IOException {
		B b = gBuffer.allocate(length+conf.getProtocolSize());
		GByteBuffer g = gBuffer.getGByteBuffer(b,true);
		this.getDataBuffer(g, connectionId, stream.getId(),segment,pos);
		stream.read(pos,length,g);
		g.writeTo(session);
	}
	
	/**
	 * for tcp mark
	 * @param stream
	 * @param index
	 */
	protected void markSegment(SegmentIdempotentStream stream,int index) {
		stream.set(index);
	}
	
	protected GByteBuffer getDataBuffer(GByteBuffer dBuffer,long connectionId,int streamId,int segment,long pos) throws IOException  {
		if(protocol!=null) {
			protocol.buildDataBuffer(dBuffer, connectionId, streamId, segment, pos);
		}else {
			defaultBuffer(dBuffer, streamId, segment, pos);
		}
		return dBuffer;
	}
	
	protected void defaultBuffer(GByteBuffer dBuffer,int streamId,int segment,long pos) throws IOException {
		dBuffer.writeString("DATA");
		dBuffer.writeInt(streamId);
		dBuffer.writeInt(segment);
		dBuffer.writeLong(pos);
	}
	
	public TransportConf geTransportConf() {
		return this.conf;
	}
	
	@Override
	public TransportIoHandler<E> getIoHandler() {
		return handler;
	}
	
	public void setIoHandler(TransportIoHandler<E> ioHandler) {
		this.handler = ioHandler;
	}
	
	@Override
	public TransportListener getTransportListener() {
		TransportIoHandler<E> handler = getIoHandler();
		if(handler==null) {
			return null;
		}
		return handler.getListener();
	}

	public void setTransportListener(TransportListener listener) {
		TransportIoHandler<E> handler = getIoHandler();
		if(handler!=null) {
			handler.setListener(listener);
		}
	}

	public DataProtocol<?> getProtocol() {
		return protocol;
	}

	public void setProtocol(DataProtocol<?> protocol) {
		this.protocol = protocol;
	}
}
