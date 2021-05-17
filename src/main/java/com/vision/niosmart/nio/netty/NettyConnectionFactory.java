package com.vision.niosmart.nio.netty;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.transport.TransportConf;

import io.netty.channel.Channel;

public class NettyConnectionFactory implements NioConnectionFactory<Channel> {
	@SuppressWarnings("unused")
	private TransportConf conf;
	
	public NettyConnectionFactory(TransportConf conf) {
		this.conf = conf;
	}
	
	@Override
	public NioConnection getConnection(Channel s)  {
		return new NettyConnection(s);
	}
	
//	@Override
//	public void releaseConnection(NioConnection con) {
//		con.useNative(null);
//	}

}
