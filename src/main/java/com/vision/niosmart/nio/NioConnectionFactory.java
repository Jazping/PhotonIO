package com.vision.niosmart.nio;

public interface NioConnectionFactory<N> {
	NioConnection getConnection(N n);
//	void releaseConnection(NioConnection con);
}
