package com.vision.niosmart.nio;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.StreamRequestEntity;
import com.vision.niosmart.stream.StreamUtils;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.util.NioBufferUtils;
import com.vision.niosmart.util.RequestBufferUtils;

public class NioWriteListener<B,E extends Protocol> extends NioListener{
	private DataTransport<E> transport;
	private BufferFactory<B> factory;
	
	public NioWriteListener(DataBufferFactory dbf,BufferFactory<B> b,DataTransport<E> transport, Context context,boolean checkId) {
		super(dbf,context,checkId);
		this.factory = b;
		this.transport = transport;
	}
	
	public NioWriteListener(DataBufferFactory dbf,BufferFactory<B> b,DataTransport<E> transport, Context context) {
		this(dbf,b,transport, context, false);
	}
	
	@Override
	protected boolean onBuffer(long id, NioConnection session, GByteBuffer buffer) throws IOException {
		return super.headRequest(id, "WRITE", buffer, (line,headers)->{
			DataBuffer db = dbf.getDataBuffer();
			try {
				int segmentSize = transport.geTransportConf().getMinRead();
				StreamRequestEntity stream = StreamUtils.parseRequestLine(id,line,segmentSize);
				if(isAcceptable(stream,session.getRemoteAddress())) {
					context.askStream(id, stream.getStream(), stream.getSegments(), stream.getName(), segmentSize, stream.getLength());
					String status = stream.isUdp()?"HTTP/3.0 200 UDP "+stream.getName():"HTTP/3.0 200 "+stream.getName();
					if(stream.isUdp()) {
						status = new StringBuilder(status).append(" ").append(session.getId()).toString();
						context.getConnection(id).setClientId(stream.getClientId());
					}
					RequestBufferUtils.notifyBuffer(status, null, db);
					NioBufferUtils.writeRetry(session, db, factory);
					session.flush();
				}
				return true;
			}catch(Throwable e) {
				e.printStackTrace();
				return true;
			}finally {
				db.release();
			}
		},false);
	}
	
	/**
	 * if return true, that will be accept remote data here. false to reject the remote data.
	 * @param stream
	 * @param remoteAddress
	 * @return
	 */
	protected boolean isAcceptable(StreamRequestEntity stream,InetSocketAddress remoteAddress) {
		return true;
	}
}
