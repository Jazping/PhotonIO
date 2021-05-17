package com.vision.niosmart.nio.netty;

import com.vision.niosmart.nio.NioAbstractFactory;
import com.vision.niosmart.nio.NioAcceptor;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.NioConnector;
import com.vision.niosmart.nio.NioFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

public class NettyFactory extends NioAbstractFactory<Channel,ByteBuf> implements NioFactory{
	private ByteBufAllocator allocator;
	
	public NettyFactory(BufferFactory<ByteBuf> factory,ByteBufAllocator allocator,NioConnectionFactory<Channel> pFactory) {
		super(factory,pFactory);
		this.allocator = allocator;
	}

	@Override
	public NioAcceptor getAcceptor(NioConfiguration cfg) {
		return new NettyAcceptor().init(cfg,factory,allocator,pfactory);
	}

	@Override
	public NioConnector getConnector(NioConfiguration cfg) {
		return new NettyConnector().init(cfg,factory,allocator,pfactory);
	}

}
