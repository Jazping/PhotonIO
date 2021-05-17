package com.vision.niosmart.nio.netty;

import java.net.SocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.NioAbstractHandler;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.StreamProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;

class NettyHandler extends NioAbstractHandler<ByteBuf> implements ChannelInboundHandler,ChannelOutboundHandler{
	protected NioConnectionFactory<Channel> pFactory;
	
	public NettyHandler(Context context, StreamProtocol protocol,
			BufferFactory<ByteBuf> factory,
			NioConnectionFactory<Channel> pfactory) {
		super(context, protocol,factory);
		this.pFactory = pfactory;
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		GByteBuffer gb = factory.getGByteBuffer(buf,false);
		NioConnection connection = pFactory.getConnection(ctx.channel());
		boolean bk = false;
		try {
			bk = super.doRead(connection, gb);
		}catch(Exception e) {
			logger.error(e.getMessage(),e);
			byte[] buff = "HTTP/3.0 500\r\n".getBytes();
			ByteBuf res = factory.allocate(buff.length+30);
			res.writeShort(6);
			res.writeBytes("NOTIFY".getBytes());
			res.writeShort(buff.length);
			res.writeBytes(buff);
			connection.write(res);
			connection.flush();
		}finally {
			if(!bk) {
				gb.release();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		NettyConnection connection = (NettyConnection) pFactory.getConnection(ctx.channel());
		connection.caughtThrow(cause);
		ctx.fireExceptionCaught(cause);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		
	}
	
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelInactive();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelReadComplete();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelWritabilityChanged();
	}

	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		ctx.bind(localAddress, promise);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
			ChannelPromise promise) throws Exception {
		ctx.connect(remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		ctx.disconnect(promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		ctx.close(promise);
	}

	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		ctx.deregister(promise);
	}

	@Override
	public void read(ChannelHandlerContext ctx) throws Exception {
		ctx.read();
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		ctx.write(msg, promise);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

}
