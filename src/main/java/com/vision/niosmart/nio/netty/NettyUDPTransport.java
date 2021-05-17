package com.vision.niosmart.nio.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.transport.AbstractUDPDataTransport;
import com.vision.niosmart.transport.DefaultTransportConf;
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
import io.netty.channel.socket.nio.NioDatagramChannel;

public class NettyUDPTransport<E extends Protocol> extends AbstractUDPDataTransport<ByteBuf,Channel,E> implements UDPDataTransport<E>{
	private Bootstrap acceptor;
	private ByteBufAllocator allocator;
	
	public NettyUDPTransport(EventLoopGroup group,Context context,
			InetSocketAddress localReceive,boolean server,
			BufferFactory<ByteBuf> f,
			DataBufferFactory pdf,
			NioConnectionFactory<Channel> cf,
			DataProtocol<E> dp,ByteBufAllocator allocator) throws IOException {
		this(group, context, new DefaultTransportConf(READ_BUFFE_SIZE,PROTOCOL_LENGTH,
						REC_BUFFE_SIZE,REC_BUFFE_SIZE),
				WAIT_EVERY_BATCH,localReceive, server,f,pdf,cf,dp,allocator);
	}
	
	public NettyUDPTransport(EventLoopGroup group,Context context,TransportConf conf, int batchWait,
			InetSocketAddress localReceive,boolean server,
			BufferFactory<ByteBuf> f,
			DataBufferFactory pdf,
			NioConnectionFactory<Channel> cf,
			DataProtocol<E> dp,ByteBufAllocator allocator) throws IOException {
		super(group,context,conf,batchWait,localReceive,server,f,pdf,cf,dp);
		this.allocator = allocator;
		this.init(group, cf, pdf, dp);
	}
	
	protected NioConnection getConnection(InetSocketAddress destination) {
		ChannelFuture future = this.acceptor.connect(destination);
//		future.awaitUninterruptibly();
		return new NettyConnection(future.channel());
	}

//	@Override
	protected void init(Executor group, NioConnectionFactory<Channel> cf, DataBufferFactory pdf, DataProtocol<E> dp) {
		this.acceptor = new Bootstrap();
		super.setIoHandler(new NettyUdpHandler<>(this,context,gBuffer,cf,pdf,dp));
		this.acceptor.group((EventLoopGroup)group)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_BROADCAST, true)
        .handler((NettyUdpHandler<E>)super.getIoHandler());
		int individual = conf.getMinRead()+conf.getProtocolSize();
		RecvByteBufAllocator recv = new AdaptiveRecvByteBufAllocator(individual,conf.getNormalRead(),conf.getMaxRead());
		this.acceptor.option(ChannelOption.ALLOCATOR, allocator);
		this.acceptor.option(ChannelOption.RCVBUF_ALLOCATOR, recv);
		this.acceptor.option(ChannelOption.SO_REUSEADDR, true);
		this.acceptor.option(ChannelOption.SO_RCVBUF, conf.getReceiveBuffer());
		this.acceptor.option(ChannelOption.SO_SNDBUF, conf.getSendBuffer());
		this.acceptor.bind(localReceive);
		logger.info("Udp Bound on {}",localReceive);
	}

	@Override
	protected ByteBuf allocate(byte[] b) {
		return allocator.buffer(b.length).clear().writeBytes(b);
	}

}
