package com.vision.niosmart.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.protocol.StreamProtocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.TransportListener;
import com.vision.niosmart.transport.UDPDataTransport;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;

public class Parameters<B> {
	private Supplier<TransportListener> listenerSpl;
	private Supplier<Executor> workerSpl;
	private Consumer<Executor> shutDownCsm;
	private BufferFactory<B> bufferFactory;
	private DataBufferFactory dataBufferFactory;
	private Function<NioConfiguration, StreamProtocol> streamProtocolFn;
	private Function<NioConfiguration, DataTransport<? extends Protocol>> dataTransportFn;
	private Function<NioConfiguration, UDPDataTransport<? extends Protocol>> udpDataTransportFn;
	private NioFactory nioFactory;
	private InetSocketAddress remoteAddress;
	private InetSocketAddress localAddress;
	private InetSocketAddress bindAddress;
	private int delay;
	private SslContext sslContext;
	private ByteBufAllocator alloc;
	public Supplier<TransportListener> getListenerSpl() {
		return listenerSpl;
	}
	public Parameters<B> setListenerSpl(Supplier<TransportListener> listenerSpl) {
		this.listenerSpl = listenerSpl;
		return this;
	}
	public Supplier<Executor> getWorkerSpl() {
		return workerSpl;
	}
	public Parameters<B> setWorkerSpl(Supplier<Executor> workerSpl) {
		this.workerSpl = workerSpl;
		return this;
	}
	public Consumer<Executor> getShutDownCsm() {
		return shutDownCsm;
	}
	public Parameters<B> setShutDownCsm(Consumer<Executor> shutDownCsm) {
		this.shutDownCsm = shutDownCsm;
		return this;
	}
	public BufferFactory<B> getBufferFactory() {
		return bufferFactory;
	}
	public Parameters<B> setBufferFactory(BufferFactory<B> bufferFactory) {
		this.bufferFactory = bufferFactory;
		return this;
	}
	public DataBufferFactory getDataBufferFactory() {
		return dataBufferFactory;
	}
	public Parameters<B> setDataBufferFactory(DataBufferFactory dataBufferFactory) {
		this.dataBufferFactory = dataBufferFactory;
		return this;
	}
	public Function<NioConfiguration, StreamProtocol> getStreamProtocolFn() {
		return streamProtocolFn;
	}
	public Parameters<B> setStreamProtocolFn(Function<NioConfiguration, StreamProtocol> streamProtocolFn) {
		this.streamProtocolFn = streamProtocolFn;
		return this;
	}
	public Function<NioConfiguration, DataTransport<? extends Protocol>> getDataTransportFn() {
		return dataTransportFn;
	}
	public Parameters<B> setDataTransportFn(Function<NioConfiguration, DataTransport<? extends Protocol>> dataTransportFn) {
		this.dataTransportFn = dataTransportFn;
		return this;
	}
	public Function<NioConfiguration, UDPDataTransport<? extends Protocol>> getUdpDataTransportFn() {
		return udpDataTransportFn;
	}
	public Parameters<B> setUdpDataTransportFn(Function<NioConfiguration, UDPDataTransport<? extends Protocol>> udpDataTransportFn) {
		this.udpDataTransportFn = udpDataTransportFn;
		return this;
	}
	public NioFactory getNioFactory() {
		return nioFactory;
	}
	public Parameters<B> setNioFactory(NioFactory nioFactory) {
		this.nioFactory = nioFactory;
		return this;
	}
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	public Parameters<B> setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}
	public Parameters<B> setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
		return this;
	}
	public int getDelay() {
		return delay;
	}
	public Parameters<B> setDelay(int delay) {
		this.delay = delay;
		return this;
	}
	public InetSocketAddress getBindAddress() {
		return bindAddress;
	}
	public Parameters<B> setBindAddress(InetSocketAddress bindAddress) {
		this.bindAddress = bindAddress;
		return this;
	}
	public SslContext getSslContext() {
		return sslContext;
	}
	public void setSslContext(SslContext sslContext) {
		this.sslContext = sslContext;
	}
	public ByteBufAllocator getAlloc() {
		return alloc;
	}
	public void setAlloc(ByteBufAllocator alloc) {
		this.alloc = alloc;
	}
}
