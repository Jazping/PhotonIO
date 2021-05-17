package com.vision.niosmart.nio.netty;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioExceptionHandler;
import com.vision.niosmart.util.RandomUtils;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

class NettyConnection implements NioConnection {
	private Channel connection;
//	private boolean closed;
	
	public NettyConnection() {}
	
	public NettyConnection(Channel connection) {
		super();
		this.connection = connection;
	}

	@Override
	public void close() {
		connection.close();
		connection.disconnect();
//		closed =  true;
	}

	@Override
	public long getId() {
		if(this.hasAttribute("connectionId")) {
			return this.getAttribute("connectionId", -1L);
		}
		return nioId(connection);//return server side id
	}
	
	static Set<Long> idSet = new HashSet<>(); 
	static synchronized long nioId(Channel channel) {
		if(channel.hasAttr(AttributeKey.valueOf("connectionId"))) {
			return (long) channel.attr(AttributeKey.valueOf("connectionId")).get();
		}
		long id  = RandomUtils.randomPositiveLong(9);
		while(!idSet.add(id)) {
			id  = RandomUtils.randomPositiveLong(9);
		}
		channel.attr(AttributeKey.valueOf("connectionId")).set(id);
		return id;
	}
	
	@Override
	public boolean isClosed() {
		return !connection.isOpen();
	}

	@Override
	public void write(Object message) {
		connection.write(message);
	}

	@Override
	public Object getNative() {
		return connection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String key, T def) {
		if(!connection.hasAttr(AttributeKey.valueOf(key))) {
			this.setAttribute(key, def);
		}
		return (T)connection.attr(AttributeKey.valueOf(key)).get();
	}

	@Override
	public void setAttribute(String key, Object value) {
		connection.attr(AttributeKey.valueOf(key)).set(value);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		InetSocketAddress address = (InetSocketAddress) connection.remoteAddress();
		if(address==null) {
			address = this.getAttribute("SENDER", null);
		}
		return address;
	}
	
	@Override
	public void flush() {
		connection.flush();
	}

//	@Override
//	public void useNative(Object n) {
//		if(n!=null&&!Channel.class.isInstance(n)) {
//			throw new ClassCastException("reqired '"+Channel.class.getName()+"'");
//		}
//		this.connection = (Channel) n;
//		this.closed = n==null;
//	}

	@Override
	public int hashCode() {
		return connection.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(NettyConnection.class.isInstance(obj)) {
			NettyConnection connection = (NettyConnection) obj;
			return this.hashCode() == connection.hashCode();
		}
		return super.equals(obj);
	}

	@Override
	public boolean isEstablished() {
		return connection.hasAttr(AttributeKey.valueOf("CONNECTED"));
	}

	@Override
	public void establish() {
		connection.attr(AttributeKey.valueOf("CONNECTED")).set(true);
	}

	@Override
	public boolean hasAttribute(String key) {
		return connection.hasAttr(AttributeKey.valueOf(key));
	}

	@Override
	public void removeAttribute(String key) {
		connection.attr(AttributeKey.valueOf(key)).set(null);
	}

	@Override
	public void setExceptionHanlder(NioExceptionHandler handle) {
		connection.attr(AttributeKey.valueOf("HANDLER")).set(handle);
	}
	
	public void caughtThrow(Throwable t) {
		AttributeKey<NioExceptionHandler> key = AttributeKey.valueOf("HANDLER");
		if(connection.hasAttr(key)) {
			connection.attr(key).get().onThrow(t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String key, Supplier<T> def) {
		if(this.hasAttribute(key)) {
			return (T)connection.attr(AttributeKey.valueOf(key)).get();
		}
		return this.getAttribute(key, def.get());
	}

	@Override
	public void synWrite(Object message) {
		connection.write(message).syncUninterruptibly();
	}
}
