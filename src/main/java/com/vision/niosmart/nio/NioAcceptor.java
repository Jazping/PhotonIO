package com.vision.niosmart.nio;

import java.io.IOException;
import java.net.SocketAddress;

public interface NioAcceptor {
	void bind()throws IOException;
	void bind(SocketAddress address)throws IOException;
	void unbind();
	void dispose();
}
