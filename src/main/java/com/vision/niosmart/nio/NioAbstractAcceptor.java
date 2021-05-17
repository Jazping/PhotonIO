package com.vision.niosmart.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class NioAbstractAcceptor<A,B> implements NioAcceptor{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected NioConfiguration nioConf;
	protected A acceptor;
	
	
	protected NioAbstractAcceptor() {
		super();
	}


	protected NioAbstractAcceptor(NioConfiguration nioConf, A acceptor) {
		super();
		this.nioConf = nioConf;
		this.acceptor = acceptor;
	}
}
