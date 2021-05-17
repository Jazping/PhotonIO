package com.vision.niosmart.consumer;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.TransportClientConf;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.IdempotentStreamProvider;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.ThreadUtil;

public class DefaultClientConsumers<B> extends DefaultServerConsumers<B> {
	private TransportClientConf conf;
	
	public TransportClientConf getConf() {
		return conf;
	}

	public void setConf(TransportClientConf conf) {
		this.conf = conf;
	}

	public DefaultClientConsumers(DataBufferFactory dbf, Context context, DataTransport<?> tcpt,
			UDPDataTransport<?> udpt, IdempotentStreamProvider provider, BufferFactory<B> facory) {
		super(dbf, context, tcpt, udpt, provider,facory);
		this.setListener(new TransportationListener() {
			@Override
			public void onError(long connectionId, String status, String resource, String msg) {
				logger.error("{} {} {} {}",connectionId,status,resource,msg);
			}
		});
	}
	
	@Consumer("ESTABLISH")
	protected void establish(@ID long id, NioConnection session, short len, @Param(-2)byte[] data) throws IOException {
		debug(id, len, data,"ESTABLISH",(line,headers)->{
			logger.info(line);
			if(headers==null||!headers.containsKey("MAX-SEGMENT-SIZE")||
					!headers.containsKey("REV-BUFFER")) {
				throw new ConnectionException("connect rejected for missing header");
			}
			if(udpt!=null&&!headers.containsKey("UPORT")) {
				throw new ConnectionException("connect rejected for missing header");
			}
			String segmentSizeStr = headers.get("MAX-SEGMENT-SIZE");
			if(!segmentSizeStr.matches("\\d+")) {
				throw new ConnectionException("connect rejected for illegal 'MAX-SEGMENT-SIZE'");
			}
			String serverBufferStr = headers.get("REV-BUFFER");
			if(!serverBufferStr.matches("\\d+")) {
				throw new ConnectionException("connect rejected for illegal 'REV-BUFFER'");
			}
			String portStr = headers.get("UPORT");
			if(portStr!=null&&!portStr.matches("\\d+")) {
				throw new ConnectionException("connect rejected for illegal 'UPORT'");
			}
			if(headers.containsKey(NioConnection.REMOTE_HOST)) {
				session.setAttribute(NioConnection.REMOTE_HOST, headers.get(NioConnection.REMOTE_HOST));
			}else {
				session.setAttribute(NioConnection.REMOTE_HOST, session.getRemoteAddress().toString());
			}
			session.setAttribute("connectionId", id);
			session.setAttribute("maxSegmentSize", Integer.valueOf(segmentSizeStr));
			session.setAttribute("serverBuffer", Integer.valueOf(serverBufferStr));
			conf.getContext().applyForConnectionId(id);
			Connection con = conf.getContext().getConnection(id);
			if(portStr!=null) {
				session.setAttribute(NioConnection.UPORT, Integer.valueOf(portStr));
				con.setRemoteReceivePort(Integer.valueOf(portStr));
				InetSocketAddress socketAddress = session.getRemoteAddress();
				con.setUDPTransport(udpt,socketAddress.getAddress(), con.getRemoteReceivePort());
				con.getUdpConnection(true);
			}
			ThreadUtil.notify(session.getNative());
			con.setRemoteReceiveBuf(Integer.valueOf(serverBufferStr));
			session.establish();
			return true;
		});
	} 
}
