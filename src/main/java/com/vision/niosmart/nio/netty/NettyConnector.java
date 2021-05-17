package com.vision.niosmart.nio.netty;

import java.net.InetSocketAddress;

import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.NioAbstractConnector;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.NioException;
import com.vision.niosmart.nio.TransportClientConf;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.transport.TransportConf;
import com.vision.niosmart.transport.UDPDataTransport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

class NettyConnector extends NioAbstractConnector<Bootstrap,ByteBuf>{
	
	NettyConnector init(NioConfiguration conf,BufferFactory<ByteBuf> factory,
			ByteBufAllocator allocator,NioConnectionFactory<Channel> pfactory) {
		this.nioConf = conf;
		this.connector = new Bootstrap();
		this.factory = factory;
		if(!EventLoopGroup.class.isInstance(nioConf.getWorkerExecutor())) {
			throw new NioException("EventLoopGroup required, not Executor");
		}
		Class<? extends SocketChannel> c = null;
		if(EpollEventLoopGroup.class.isInstance(nioConf.getWorkerExecutor())) {
			c = EpollSocketChannel.class;
		}else {
			c = NioSocketChannel.class;
		}
		NettyHandler handler = new NettyHandler(nioConf.getContext(),nioConf.getProtocol(),factory,pfactory);
		ChannelInitializer channels = new ChannelInitializer(nioConf.getSslContext(),nioConf.getAlloc(),handler);;
		connector.group((EventLoopGroup)nioConf.getWorkerExecutor())
		.localAddress(nioConf.getBindAddress()).remoteAddress(nioConf.getRemoteAddress())
        .channel(c).handler(channels);
		if(UDPDataTransport.class.isInstance(nioConf.getTransport())) {
			logger.info("Connector initalized on socket address {}",nioConf.getBindAddress());
		}
		return config(allocator);
	}
	
	private NettyConnector config(ByteBufAllocator allocator) {
		TransportConf conf = checkConfiguration();
		int individual = conf.getMinRead()+conf.getProtocolSize();
		RecvByteBufAllocator recv = new AdaptiveRecvByteBufAllocator(individual,conf.getNormalRead(),conf.getMaxRead());
		TransportClientConf cConf = nioConf.getClientConf();
		connector.option(ChannelOption.SO_RCVBUF, conf.getReceiveBuffer())
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 8000)
		.option(ChannelOption.SO_SNDBUF, conf.getSendBuffer())
        .option(ChannelOption.SO_KEEPALIVE, nioConf.isKeepAlive())
		.option(ChannelOption.ALLOCATOR, allocator)
		.option(ChannelOption.RCVBUF_ALLOCATOR, recv);
		InetSocketAddress localAddr = cConf.getBindAddress();
		logger.info("Connector initalized on socket address {}",localAddr);
		return this;
	}

	@Override
	protected NioConnection newConnection() {
		ChannelFuture cf = this.connector.connect().syncUninterruptibly();
		if(!cf.awaitUninterruptibly(8000)) {
			throw new ConnectionException("Connect timeout");
		}
		if(cf.isSuccess()) {
			Channel channel = cf.channel();
			if(channel!=null&&channel.isOpen()&&channel.isActive()&&channel.isWritable()) {
				return new NettyConnection(channel);
			}
		}else {
			logger.error(cf.cause().getMessage(),cf.cause());
		}
		throw new ConnectionException("Connection refused");
	}
}
