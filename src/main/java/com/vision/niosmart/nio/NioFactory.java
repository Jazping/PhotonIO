package com.vision.niosmart.nio;

public interface NioFactory {
	NioAcceptor getAcceptor(NioConfiguration cfg);
	NioConnector getConnector(NioConfiguration cfg);
}
