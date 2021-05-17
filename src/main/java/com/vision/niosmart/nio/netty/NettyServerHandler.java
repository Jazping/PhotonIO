package com.vision.niosmart.nio.netty;

import java.net.InetSocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.protocol.StreamProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

class NettyServerHandler extends NettyHandler{

	public NettyServerHandler(Context context, StreamProtocol protocol,BufferFactory<ByteBuf> factory,NioConnectionFactory<Channel> pfactory) {
		super(context, protocol, factory,pfactory);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		this.clean((SocketChannel) ctx.channel());
//		logger.error(cause.getMessage(),cause);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		this.clean((SocketChannel) ctx.channel());
	}
	
	private void clean(SocketChannel channel) {
		long id = NettyConnection.nioId(channel);
		InetSocketAddress address = channel.remoteAddress();
		context.removeAddress(address);
		context.closeConnection(id);
		NettyConnection.idSet.remove(id);
		channel.close();
		channel.disconnect();
		logger.info("channel {} disconnected",id);
	}

}
