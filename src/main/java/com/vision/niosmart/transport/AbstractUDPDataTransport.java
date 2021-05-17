package com.vision.niosmart.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.StreamUtils;


public abstract class AbstractUDPDataTransport<B,C,E extends Protocol> extends AbstractDataTransport<B,E> implements UDPDataTransport<E>{
	protected Map<InetSocketAddress, NioConnection> cacheMap = new HashMap<>();
	protected InetSocketAddress localReceive;
	protected int batchWait;
	
	protected AbstractUDPDataTransport(Executor group,Context context,
			InetSocketAddress localReceive,boolean server,
			BufferFactory<B> f,
			DataBufferFactory pdf,
			NioConnectionFactory<C> cf,
			DataProtocol<E> dp) throws IOException {
		this(group, context, 
				new DefaultTransportConf(READ_BUFFE_SIZE,PROTOCOL_LENGTH,
						REC_BUFFE_SIZE,REC_BUFFE_SIZE),
				WAIT_EVERY_BATCH,localReceive, server,f,pdf,cf,dp);
	}
	
	protected AbstractUDPDataTransport(Executor group,Context context,TransportConf conf, int batchWait,
			InetSocketAddress localReceive,boolean server,
			BufferFactory<B> f,
			DataBufferFactory pdf,
			NioConnectionFactory<C> cf,
			DataProtocol<E> dp) throws IOException {
		super(group,context,conf,f,pdf);
		this.localReceive = localReceive;
		this.batchWait = batchWait;
		this.protocol = dp;
		if(batchWait<50) {
			throw new IllegalArgumentException("batch wait 50+");
		}
	}
	
	protected abstract NioConnection getConnection(InetSocketAddress destination);
	
	@Override
	protected void markSegment(SegmentIdempotentStream stream,int index) {
		
	}
	
	@Override
	public void close() {
		for(NioConnection session : this.cacheMap.values()) {
			session.close();
		}
		this.cacheMap.clear();
	}

	@Override
	public int getReceivePort() {
		return localReceive.getPort();
	}

	@Override
	public void removeCache(InetSocketAddress destination) {
		if(cacheMap.containsKey(destination)) {
			synchronized(cacheMap) {
				if(cacheMap.containsKey(destination)) {
					cacheMap.remove(destination);
				}
			}
		}
	}
	@Override
	public int getBatchWait() {
		return batchWait;
	}

	@Override
	public void send(int type, long connectionId, int streamId, int segment, byte[] data,
			int off, int length,NioConnection session) {
		@SuppressWarnings("deprecation")
		byte[]b = StreamUtils.toSegmentBuf(type,connectionId, streamId, segment, data,off,length);
		session.write(allocate(b));
	}
	
	protected void send(ProtocolDatagram datagram,NioConnection session) {
		@SuppressWarnings("deprecation")
		byte[]b = StreamUtils.toSegmentBuf(datagram.getType(),datagram.getConnectionId(),
				datagram.getStreamId(), datagram.getSegment(), datagram.getData(),datagram.getOff(),datagram.getLength());
		session.write(allocate(b));
	}
	
	protected abstract B allocate(byte[]b);
	
	@Override
	public int transport(NioConnection session,SegmentIdempotentStream stream) throws IOException {
		int index = -1;
		if(!stream.isClosed()) {
			Connection con = context.getConnection(session.getId());
			if(con!=null&&con.getUdpConnection(false)!=null) {
				index = transport(stream,con.getUdpConnection(false),session);
			}
		}
		return index;
	}
	
	private final int transport(SegmentIdempotentStream stream,NioConnection session,NioConnection tcpSession) throws IOException {
		synchronized (stream) {
			Connection con = context.getConnection(tcpSession.getId());
			if(con==null) {
				logger.error("Drop transport connection {} stream {}",stream.getConnectionId(),stream.getId());
				return -1;
			}
			if(stream.allTrue()||stream.isClosed()||tcpSession.isClosed()) {
				return -1;
			}
			int batch = con.getFixBatchSize();
			batch = Math.min(batch, stream.remaining());
			if(stream.getTransportMonitor()==null) {
				stream.setTransportMonitor(new TransportMonitor(batch));
			}
			TransportMonitor monitor = stream.getTransportMonitor();
			monitor.setBathSize(batch);
			int segmentPos = batchSend(stream,0,batch,session);
			if(segmentPos!=-1) {
				monitor.move(segmentPos);
			}
			if(monitor.shouldBeGiveup()&&!stream.allTrue()) {
				logger.warn("Transport give up connection {} stream {} remaining {}",stream.getConnectionId(),stream.getId(),stream.remaining());
			}
			return monitor.shouldBeGiveup()?-1:batch;
		}
	}
	
	protected InetSocketAddress getUdpAddress(NioConnection tc) {
		InetSocketAddress destAddr = (InetSocketAddress) tc.getRemoteAddress();
		if(!tc.hasAttribute(NioConnection.UPORT)) {
			throw new ConnectionException("client not suported udp transported");
		}
		return new InetSocketAddress(destAddr.getAddress(),tc.getAttribute(NioConnection.UPORT, 0));
	}
	
	@Override
	public NioConnection getUdpConnection(InetAddress address, int port) {
		return this.getConnection(new InetSocketAddress(address, port));
	}
	
//	private final int transport(SegmentIdempotentStream stream,NioConnection session,NioConnection tcpSession) throws IOException {
//		synchronized (stream) {
//			String key = stream.getConnectionId()+"&"+stream.getId();
//			Connection con = context.getConnection(stream.getConnectionId());
//			if(con==null) {
//				logger.error("drop transport connection {} stream {}",stream.getConnectionId(),stream.getId());
//				return -1;
//			}
//			int batch = con.getFixBatchSize();
//			TransportMonitor monitor = new TransportMonitor(batch);
//			monitorMap.put(key, monitor);
//			int counter = 0;
//			int segment = batchSend(stream,0,batch,session);
//			while(counter<3&&!stream.isClosed()&&!tcpSession.isClosed()&&stream.remaining()>0) {
//				session.flush();
//				ThreadUtil.wait(stream, batchWait);
//				if(!stream.isClosed()&&!tcpSession.isClosed()&&stream.remaining()>0) {
//					batch = con.getFixBatchSize();
//					monitor.setBathSize(batch);
//					int p2 = batchSend(stream,0,batch,session);
//					if(p2==segment) {
//						System.err.println(segment+" "+p2+" "+stream.getSegments()+" "+stream.remaining());
//						counter++;
//					}else {
//						counter=0;
//						segment = p2;
//					}
//				}
//			}
//			session.flush();
//			if(counter==3) {
//				logger.warn("transport give up connection {} stream length {}",stream.getConnectionId(),stream.length());
//			}
//		}
//	}
}
