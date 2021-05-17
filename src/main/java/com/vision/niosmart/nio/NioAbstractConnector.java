package com.vision.niosmart.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.TransportConf;
import com.vision.niosmart.util.NioBufferUtils;
import com.vision.niosmart.util.RequestBufferUtils;
import com.vision.niosmart.util.ThreadUtil;

public abstract class NioAbstractConnector<C,B> implements NioConnector {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected NioConfiguration nioConf;
	protected C connector;
	protected BufferFactory<B> factory;
	protected NioConnection connection;

	public NioAbstractConnector() {
		super();
	}

	protected NioAbstractConnector(NioConfiguration nioConf, C connector,BufferFactory<B> factory) {
		super();
		this.nioConf = nioConf;
		this.connector = connector;
		this.factory = factory;
	}
	
	@Override
	public NioConnection connect() {
		return getSession(nioConf.getTransport().geTransportConf(),nioConf.getClientConf());
	}

	@Override
	public void disponse() {
		if(!connection.isClosed()) {
			connection.close();
			if(nioConf.getTransport()!=null) {
				nioConf.getTransport().close();
			}
		}
	}


	@Override
	public boolean isDisposed() {
		return connection.isClosed();
	}
	
	private synchronized NioConnection getSession(TransportConf conf,TransportClientConf clientConf) {
		if(connection==null||connection.isClosed()) {
			this.connection  = this.newConnection();
			StringBuilder sb = new StringBuilder("HTTP/3.0");
			sb.append("\r\n");
			sb.append("MAX-SEGMENT-SIZE: " + conf.getMinRead());
			sb.append("\r\n");
			sb.append("REV-BUFFER: " + conf.getReceiveBuffer());
			sb.append("\r\n");
			sb.append("REMOTE-HOST: "+ clientConf.getLocalAddress());
			sb.append("\r\n");
			if(nioConf.getUdpTransport()!=null) {
				sb.append("UPORT: " + clientConf.getUport());
				sb.append("\r\n");
			}
			sb.append("HEAD-PROTOCOL: UTF('WRITE','READ','NOTIFY','CONNECT')|id(8b)|array-length(2b)|array");
			sb.append("\r\n");
			sb.append("DATA-PROTOCOL: UTF('DATA')|type(2b)|id(8b)|stream(2b)|segment(4b)|pos(8b)|data-length(2b)|data");
			sb.append("\r\n");
			if(nioConf.getUdpTransport()!=null) {
				sb.append("DATA-CONFIRM-PROTOCOL: type(2b)|id(8b)|stream(2b)|segment(4b)|data-length(2b)|1b");
				sb.append("\r\n");
			}
			
			GByteBuffer g = factory.getGByteBuffer(factory.allocate(1024), true);
			RequestBufferUtils.buffer("CONNECT", sb.toString(), null, g);
			int retry = 2;
			while(retry-->0&&!connection.isEstablished()) {
				logger.info("Try to establish connection on {}",clientConf.getRemoteAddress());
				NioBufferUtils.writeRetry(connection, g, factory);
				connection.flush();
				ThreadUtil.wait(connection.getNative(), 5000);
			}
			if(!connection.isEstablished()) {
				this.disponse();
				throw new ConnectionException("Can not get the connection from "+clientConf.getRemoteAddress()) ;
			}
			logger.info("Connection established on {}",clientConf.getRemoteAddress());
		}
		return connection;
	}
	
	protected TransportConf checkConfiguration() {
		TransportClientConf cConf = nioConf.getClientConf();
		if(cConf==null) {
			throw new NioException("TransportClientConf is required");
		}
		DataTransport<?> transport = nioConf.getTransport();
		if(transport==null) {
			throw new NioException("DataTransport is required");
		}
		TransportConf conf = transport.geTransportConf();
		if(conf==null) {
			throw new NioException("DataTransport listener not set");
		}
		if(nioConf.getProtocol()==null) {
			throw new NioException("protocol is required");
		}
		if(conf.getMinRead()<1024) {
			throw new NioException("read buffer must more then 1024 bytes");
		}
		if(conf.getProtocolSize()<0) {
			throw new NioException("nagtive protocol length");
		}
		return conf;
	}
	
	@Override
	public long currentId() {
		if(connection!=null) {
			return connection.getAttribute("connectionId", 1L);
		}
		return -1;
	}
	
	protected abstract NioConnection newConnection();
}
