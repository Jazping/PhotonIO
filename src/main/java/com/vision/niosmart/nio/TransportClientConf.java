package com.vision.niosmart.nio;

import java.net.InetSocketAddress;

import javax.net.SocketFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.transport.AbstractDataTransport;
import com.vision.niosmart.transport.TransportListener;

public class TransportClientConf {
	private Context context;
	private SocketFactory socketFactory;
	private InetSocketAddress remoteAddress;
	private InetSocketAddress localAddress;
	private InetSocketAddress bindAddress;
//	private int revBuffer = 1024*1024;
	private int metaSize = AbstractDataTransport.PROTOCOL_LENGTH;
	private TransportListener listener;
	private int uport;
	public TransportClientConf() {
		super();
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}
	public int getMetaSize() {
		return metaSize;
	}
	public void setMetaSize(int metaSize) {
		this.metaSize = metaSize;
	}
	public TransportListener getListener() {
		return listener;
	}
	public void setListener(TransportListener listener) {
		this.listener = listener;
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
	public int getUport() {
		return uport;
	}
	public void setUport(int uport) {
		this.uport = uport;
	}
	public InetSocketAddress getBindAddress() {
		return bindAddress;
	}
	public void setBindAddress(InetSocketAddress bindAddress) {
		this.bindAddress = bindAddress;
	}
}
