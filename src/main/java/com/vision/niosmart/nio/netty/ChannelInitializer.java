package com.vision.niosmart.nio.netty;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel>{
	private List<ChannelHandler> handlers = new ArrayList<ChannelHandler>();
	private SslContext sslContext;
	private ByteBufAllocator alloc;
	public ChannelInitializer(SslContext sslContext,ByteBufAllocator alloc,ChannelHandler... handlers) {
		for(ChannelHandler h : handlers) {
			this.handlers.add(h);
		}
		this.sslContext = sslContext;
		this.alloc = alloc;
	}
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		if(this.sslContext!=null&&alloc!=null) {
			ch.pipeline().addLast(this.sslContext.newHandler(alloc));
		}
		handlers.forEach((h)->ch.pipeline().addLast(h));
	}
}
