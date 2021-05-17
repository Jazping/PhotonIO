package com.vision.niosmart.client;

import com.vision.niosmart.nio.NioConfiguration;

/**
 * NioSmart client pooled factory, use to pressure testing
 * @author Jazping
 *
 * @param <S> connection type
 * @param <B> buffer type
 */
public class PooledClientFactory<S,B> extends PooledBaseClientFactory<S,B>{
	
//	public PooledClientFactory(Supplier<TransportListener> listenerSupplier,
//			Supplier<Executor> workerSupplier,
//			Consumer<Executor> shutDown,
//			BufferFactory<B> factory,
//			DataBufferFactory dbf,
//			Function<NioConfiguration, StreamProtocol> fn,
//			Function<NioConfiguration, DataTransport<? extends Protocol>> tcpFn,
//			Function<NioConfiguration, UDPDataTransport<? extends Protocol>> udpFn,
//			NioFactory nioFactory,InetSocketAddress remoteAddress,InetSocketAddress localAddress,int delay) {
//		super(listenerSupplier,workerSupplier,shutDown,factory,dbf,fn,tcpFn,udpFn,nioFactory,remoteAddress,localAddress,delay);
//	}

	/**
	 * @param Parameters<B> ps
	 */
	public PooledClientFactory(Parameters<B> ps) {
		super(ps);
	}

	protected BaseNioSmartClient<B> newInstance(NioConfiguration nioConf, ClientPool<B> pool){
		return new NioSmartClient<>(nioFactory,nioConf,factory,dbf,pool);
	}
}
