package com.vision.niosmart.nio;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.ThreadUtil;

public class NioEstablishListener extends NioListener {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private TransportClientConf conf;
	private UDPDataTransport<?> udpt;
	public NioEstablishListener(DataBufferFactory dbf,TransportClientConf conf, UDPDataTransport<?> udpt) {
		super(dbf,conf.getContext());
		this.conf = conf;
		this.udpt = udpt;
	}
	

	@Override
	public boolean onBuffer(long id,NioConnection session, GByteBuffer buffer) throws IOException {
		return super.headRequest(id, "ESTABLISH", buffer, (line,headers)->{
			logger.debug("ESTABLISH");
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
				new Thread() {
					public void run() {
						session.setAttribute(NioConnection.UPORT, Integer.valueOf(portStr));
						con.setRemoteReceivePort(Integer.valueOf(portStr));
						InetSocketAddress socketAddress = session.getRemoteAddress();
						con.setUDPTransport(udpt,socketAddress.getAddress(), con.getRemoteReceivePort());
						con.getUdpConnection(true);
						ThreadUtil.notify(session.getNative());
					}
				}.start();
				
//				session.setAttribute(NioConnection.UPORT, Integer.valueOf(portStr));
//				con.setRemoteReceivePort(Integer.valueOf(portStr));
//				InetSocketAddress socketAddress = session.getRemoteAddress();
//				con.setUdpConnection(udpt.getUdpConnection(socketAddress.getAddress(), con.getRemoteReceivePort()));
			}else {
				ThreadUtil.notify(session.getNative());
			}
			con.setRemoteReceiveBuf(Integer.valueOf(serverBufferStr));
			session.establish();
			return true;
		},false);
	}
}
