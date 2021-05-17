package com.vision.niosmart.nio;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import com.vision.niosmart.Context;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.protocol.StreamProtocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.UDPDataTransport;

public class NioConfiguration {
	private Context context;
	private int port;
	private Executor ioExecutor;
	private Executor workerExecutor;
	private DataTransport<? extends Protocol> transport;
	private UDPDataTransport<? extends Protocol> udpTransport;
	private InetSocketAddress remoteAddress;
	private InetSocketAddress localAddress;
	private InetSocketAddress bindAddress;
	private TransportClientConf clientConf;
	private StreamProtocol protocol;
	private DataBufferFactory dataBufferFactory;
	private int maxConnections = 512;
	private boolean keepAlive = true;
	private int socketTime = 3000;
	private Object sslContext;
	private Object alloc;
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Executor getIoExecutor() {
		return ioExecutor;
	}
	public void setIoExecutor(Executor ioExecutor) {
		this.ioExecutor = ioExecutor;
	}
	public Executor getWorkerExecutor() {
		return workerExecutor;
	}
	public void setWorkerExecutor(Executor workerExecutor) {
		this.workerExecutor = workerExecutor;
	}
	public DataTransport<? extends Protocol> getTransport() {
		return transport;
	}
	public void setTransport(DataTransport<? extends Protocol> transport) {
		this.transport = transport;
	}
	public TransportClientConf getClientConf() {
		return clientConf;
	}
	public void setClientConf(TransportClientConf clientConf) {
		this.clientConf = clientConf;
	}
	public StreamProtocol getProtocol() {
		return  protocol;
	}
	public void setProtocol(StreamProtocol protocol) {
		this.protocol = protocol;
	}
	public int getMaxConnections() {
		return maxConnections;
	}
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	public boolean isKeepAlive() {
		return keepAlive;
	}
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
	public int getSocketTime() {
		return socketTime;
	}
	public void setSocketTime(int socketTime) {
		this.socketTime = socketTime;
	}
	public UDPDataTransport<? extends Protocol> getUdpTransport() {
		return udpTransport;
	}
	public void setUdpTransport(UDPDataTransport<? extends Protocol> udpTransport) {
		this.udpTransport = udpTransport;
	}
	public DataBufferFactory getDataBufferFactory() {
		return dataBufferFactory;
	}
	public void setDataBufferFactory(DataBufferFactory dataBufferFactory) {
		this.dataBufferFactory = dataBufferFactory;
	}
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}
	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}
	public InetSocketAddress getBindAddress() {
		return bindAddress;
	}
	public void setBindAddress(InetSocketAddress bindAddress) {
		this.bindAddress = bindAddress;
	}
	@SuppressWarnings("unchecked")
	public <S> S getSslContext() {
		return (S)sslContext;
	}
	public <S> void setSslContext(S sslContext) {
		this.sslContext = sslContext;
	}
	@SuppressWarnings("unchecked")
	public <A> A getAlloc() {
		return (A)alloc;
	}
	public <A> void setAlloc(A alloc) {
		this.alloc = alloc;
	}
}
