package com.vision.niosmart.nio.netty;

import java.io.IOException;
import java.net.SocketAddress;

import com.vision.niosmart.nio.NioAbstractAcceptor;
import com.vision.niosmart.nio.NioAcceptor;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.NioException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.transport.TransportConf;
import com.vision.niosmart.transport.UDPDataTransport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

class NettyAcceptor extends NioAbstractAcceptor<ServerBootstrap,ByteBuf> implements NioAcceptor {
	
	NioAcceptor init(NioConfiguration nioConf,BufferFactory<ByteBuf> factory,ByteBufAllocator allocator,NioConnectionFactory<Channel> pfactory) {
		this.nioConf = nioConf;
		this.acceptor = new ServerBootstrap();
		if(!EventLoopGroup.class.isInstance(nioConf.getIoExecutor())) {
			throw new NioException("EventLoopGroup required, not Executor");
		}
		if(!EventLoopGroup.class.isInstance(nioConf.getWorkerExecutor())) {
			throw new NioException("EventLoopGroup required, not Executor");
		}
		Class<? extends ServerSocketChannel> c = null;
		if(EpollEventLoopGroup.class.isInstance(nioConf.getIoExecutor())) {
			c = EpollServerSocketChannel.class;
		}else {
			c = NioServerSocketChannel.class;
		}
		ChannelHandler h = new NettyServerHandler(nioConf.getContext(),nioConf.getProtocol(),factory,pfactory);
		ChannelHandler childHandler = new ChannelInitializer(nioConf.getSslContext(),nioConf.getAlloc(),h);
		acceptor.group((EventLoopGroup)nioConf.getIoExecutor(), (EventLoopGroup)nioConf.getWorkerExecutor())
		.channel(c).localAddress(nioConf.getLocalAddress()).childHandler(childHandler)
		.option(ChannelOption.SO_BACKLOG, nioConf.getMaxConnections())
        .childOption(ChannelOption.SO_KEEPALIVE, nioConf.isKeepAlive());
		logger.info("Creating Server with local address {}, bind address {}",nioConf.getLocalAddress(),nioConf.getBindAddress());
		if(UDPDataTransport.class.isInstance(nioConf.getTransport())) {
			logger.info("Server Data initalized receive on port {}",nioConf.getPort());
		}
		return this.config(allocator);
	}
	
	private NioAcceptor config(ByteBufAllocator allocator) {
		TransportConf conf = nioConf.getTransport().geTransportConf();
		int individual = conf.getMinRead()+conf.getProtocolSize();
		RecvByteBufAllocator recv = new AdaptiveRecvByteBufAllocator(individual,conf.getNormalRead(),conf.getMaxRead());
		acceptor.option(ChannelOption.ALLOCATOR, allocator);
		acceptor.option(ChannelOption.RCVBUF_ALLOCATOR, recv);
		acceptor.option(ChannelOption.SO_RCVBUF, conf.getReceiveBuffer())
		.childOption(ChannelOption.SO_SNDBUF, conf.getSendBuffer());
		logger.info("HTTP/3.0 100");
		logger.info("MAX-SEGMENT-SIZE: "+ conf.getMinRead());
		logger.info("REV-BUFFER: " + conf.getReceiveBuffer());
		if(nioConf.getUdpTransport()!=null) {
			UDPDataTransport<?> ut = nioConf.getUdpTransport();
			logger.info("UPORT: " + ut.getReceivePort());
		}
		logger.info("HEAD-PROTOCOL: id(8b)|UTF('WRITE','READ','NOTIFY')|data-length(2b)|data");
		logger.info("TCP-DATA-PROTOCOL: UTF('DATA')|type(2b)|id(8b)|stream(2b)|segment(4b)|pos(8b)|data-length(2b)|data");
		if(nioConf.getUdpTransport()!=null) {
			logger.info("UDP-DATA-PROTOCOL: type(2b)|id(8b)|stream(2b)|segment(4b)|pos(8b)|data-length(2b)|data");
			logger.info("DATA-CONFIRM-PROTOCOL: type(2b)|id(8b)|stream(2b)|segment(4b)|data-length(2b)|1b");
		}
		return this;
	}
	
	@Override
	public void bind() throws IOException {
		try {
			this.acceptor.bind().sync();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void unbind() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void bind(SocketAddress address) throws IOException {
		this.acceptor.bind(address);
	}
}
