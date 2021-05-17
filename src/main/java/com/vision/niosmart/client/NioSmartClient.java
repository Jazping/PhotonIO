package com.vision.niosmart.client;

import java.io.IOException;

import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioException;
import com.vision.niosmart.nio.NioFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.util.NioBufferUtils;
import com.vision.niosmart.util.RequestBufferUtils;
/**
 * nio smart client for data transport, provide datagram sending, stream request, and stream writting
 * @author Jazping
 *
 * @param <B> which nio type to use
 */
public class NioSmartClient<B> extends BaseNioSmartClient<B> {
	
	public NioSmartClient(ClientPool<B> pool) {
		super(pool);
	}
	
	public NioSmartClient(NioFactory factor, NioConfiguration configuration,
			BufferFactory<B> bfactory,DataBufferFactory dbf,ClientPool<B> pool) {
		super(factor,configuration,bfactory,dbf,pool);
	}

	/**
	 * write an IdempotentStream to server, mybe using tcp or udp
	 * @param stream
	 * @param useUdp
	 * @throws IOException
	 */
	public final void write(IdempotentStream stream,boolean useUdp) throws IOException{
		NioConnection session = connect();
		askStream(session,stream,stream.getId(),useUdp);
	}
	
	/**
	 * request resource from server, mybe using tcp or udp
	 * @param name
	 * @param headers
	 * @param useUdp
	 * @throws IOException
	 */
	public final void read(String name,StringBuilder headers,boolean useUdp) throws IOException {
		NioConnection session = connect();
		configuration.getContext().putGetting(name);
		GByteBuffer g = bfactory.getGByteBuffer(bfactory.allocate(1024), true);
		RequestBufferUtils.readBuffer(name, headers, useUdp, session.getId(), g);
		NioBufferUtils.writeRetry(session, g, bfactory);
	}
	
	/**
	 * blocking and await all operation completed and shutdown client
	 */
	public final synchronized void awaitAndShutdown() {
		if(this.connector==null||this.connector.isDisposed()) {
			return;
		}
		awaitComplete(60000);
		this.shutdown();
	}
	
	private final SegmentIdempotentStream askStream(NioConnection session,IdempotentStream stream,int id,boolean udp) throws IOException {
		Long connectionId = (Long) session.getAttribute("connectionId",0L);
		Integer maxSegmentSize = (Integer) session.getAttribute("maxSegmentSize",4096);
		SegmentIdempotentStream info = putDatagramStream(udp, connectionId, stream, maxSegmentSize);
		GByteBuffer g = bfactory.getGByteBuffer(bfactory.allocate(1024), true);
		RequestBufferUtils.writeBuffer(id,info.getSegments(),stream.name(),stream.length(),null, udp, session.getId(), g);
		NioBufferUtils.writeRetry(session, g, bfactory);
		return info;
	}
	
	private SegmentIdempotentStream putDatagramStream(boolean udp, long connectionId, IdempotentStream stream, 
			int segmentSize) throws IOException {
		if(configuration.getTransport()==null) {
			throw new NioException("tcp transport not supported");
		}
		if(configuration.getUdpTransport()==null) {
			throw new NioException("udp transport not supported");
		}
		return udp?configuration.getContext().putDatagramStreamUDP(connectionId, stream.name(),stream, segmentSize):
			configuration.getContext().putDatagramStream(connectionId, stream.name(),stream, segmentSize);
	}
	
}
