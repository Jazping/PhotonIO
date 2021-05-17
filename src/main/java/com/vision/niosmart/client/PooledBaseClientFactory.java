package com.vision.niosmart.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.vision.niosmart.DefaultContext;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioFactory;
import com.vision.niosmart.nio.TransportClientConf;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.protocol.StreamProtocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.TransportListener;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.ThreadUtil;

public class PooledBaseClientFactory<S,B> extends BasePooledObjectFactory<BaseNioSmartClient<B>>  implements PooledObjectFactory<BaseNioSmartClient<B>>{
	protected BufferFactory<B> factory;
	protected NioFactory nioFactory;
	protected DataBufferFactory dbf;
	protected InetSocketAddress remoteAddress;
	protected InetSocketAddress localAddress;
	protected InetSocketAddress bindAddress;
	protected Supplier<TransportListener> listenerSupplier;
	protected Function<NioConfiguration, StreamProtocol> fn;
	protected Supplier<Executor> workerSupplier;
	protected Consumer<Executor> shutDown;
	protected Function<NioConfiguration, DataTransport<? extends Protocol>> tcpFn;
	protected Function<NioConfiguration, UDPDataTransport<? extends Protocol>> udpFn;
	protected int delay = 0;
	protected Parameters<B> ps;
	
	private ClientPool<B> pool;
	
	/**
	 * @param Parameters<B> ps
	 */
	public PooledBaseClientFactory(Parameters<B> ps) {
		this.fn = ps.getStreamProtocolFn();
		this.tcpFn = ps.getDataTransportFn();
		this.udpFn = ps.getUdpDataTransportFn();
		this.dbf = ps.getDataBufferFactory();
		this.shutDown = ps.getShutDownCsm();
		this.workerSupplier = ps.getWorkerSpl();
		this.remoteAddress = ps.getRemoteAddress();
		this.listenerSupplier = ps.getListenerSpl();
		this.factory = ps.getBufferFactory();
		this.nioFactory = ps.getNioFactory();
		this.delay = ps.getDelay();
		this.localAddress = ps.getLocalAddress();
		this.bindAddress = ps.getBindAddress();
		this.ps = ps;
	}

	@Override
	public void destroyObject(PooledObject<BaseNioSmartClient<B>> p) throws Exception {
		super.destroyObject(p);
		p.getObject().clean();
		p.getObject().shutdown();
		shutDown.accept(p.getObject().getConfiguration().getIoExecutor());
	}

	@Override
	public synchronized BaseNioSmartClient<B> create() throws Exception {
		if(delay>0) {
			ThreadUtil.sleep(this, delay);
		}
		TransportClientConf clientConf = new TransportClientConf();
		clientConf.setRemoteAddress(remoteAddress);
		clientConf.setLocalAddress(localAddress);
		clientConf.setBindAddress(bindAddress);
		clientConf.setContext(new DefaultContext());
		clientConf.setListener(listenerSupplier==null?null:listenerSupplier.get());
		NioConfiguration nioConf = new NioConfiguration();
		nioConf.setPort(remoteAddress.getPort());
		nioConf.setClientConf(clientConf);
		nioConf.setDataBufferFactory(dbf);
		nioConf.setContext(clientConf.getContext());
		nioConf.setLocalAddress(localAddress);
		nioConf.setRemoteAddress(remoteAddress);
		nioConf.setBindAddress(bindAddress);
		Executor executor = workerSupplier.get();
		nioConf.setIoExecutor(executor);
		nioConf.setWorkerExecutor(executor);
		nioConf.setTransport(tcpFn.apply(nioConf));
		nioConf.setUdpTransport(udpFn==null?null:udpFn.apply(nioConf));
		nioConf.setProtocol(fn.apply(nioConf));
		nioConf.setKeepAlive(true);
		nioConf.setSslContext(ps.getSslContext());
		nioConf.setAlloc(ps.getAlloc());
		UDPDataTransport<? extends Protocol> udpt = nioConf.getUdpTransport();
		clientConf.setUport(udpt==null?0:udpt.getReceivePort());
		BaseNioSmartClient<B> client = newInstance(nioConf,pool);
		client.setExceptionHandler("Base",IOException.class,(throwable)->pool.dropObject(client));
		return client;
	}
	
	protected BaseNioSmartClient<B> newInstance(NioConfiguration nioConf,ClientPool<B> pool){
		return new BaseNioSmartClient<>(nioFactory,nioConf,factory,dbf,pool);
	}

	@Override
	public PooledObject<BaseNioSmartClient<B>> wrap(BaseNioSmartClient<B> obj) {
		return new DefaultPooledObject<BaseNioSmartClient<B>>(obj);
	}

	public ClientPool<B> getPool() {
		return pool;
	}

	public void setPool(ClientPool<B> pool) {
		this.pool = pool;
	}
}
