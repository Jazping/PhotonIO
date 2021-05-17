package com.vision.niosmart.nio.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.transport.UDPIOHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

class NettyUdpHandler<E extends Protocol> extends UDPIOHandler<E> implements ChannelInboundHandler,ChannelOutboundHandler{
	private BufferFactory<ByteBuf> factory;
	private NioConnectionFactory<Channel> pfactory;
	
	public NettyUdpHandler(UDPDataTransport<E> transport, Context context, 
			BufferFactory<ByteBuf> factory,
			NioConnectionFactory<Channel> pfactory,
			DataBufferFactory dbf,
			DataProtocol<E> dp) {
		super(transport, context, dbf,dp);
		this.factory = factory;
		this.pfactory = pfactory;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(PortUnreachableException.class.isInstance(cause)) {
			long id = NettyConnection.nioId(ctx.channel());
			InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
			context.removeAddress(address);
			context.closeConnection(id);
			NettyConnection.idSet.remove(id);
			ctx.channel().close();
			ctx.channel().disconnect();
			logger.debug("channel {} disconnected",new NettyConnection(ctx.channel()).getId());
		}else {
			ctx.fireExceptionCaught(cause);
		}
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

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket packet = (DatagramPacket) msg;
		ByteBuf buffer = (ByteBuf) packet.content();
		GByteBuffer gb = factory.getGByteBuffer(buffer,false);
		NioConnection con = pfactory.getConnection(ctx.channel());
		try {
			con.setAttribute("SENDER", packet.sender());
			while(gb.hasRemaining()&&!con.isClosed()) {
				int type = gb.readShort();
				if(type==0) {
					confirm(gb);
				}else if(type==1){
					super.doReceive(con, gb, gb.readLong());
				}else {
					logger.warn("illegal buffer type {} and drop this packet",type);
				}
			}
		}catch(IOException e) {
			if(!con.isClosed()) {
				throw e;
			}
		}finally {
			packet.release();
		}
	}

}
