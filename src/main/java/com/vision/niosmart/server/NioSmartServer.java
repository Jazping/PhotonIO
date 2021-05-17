package com.vision.niosmart.server;

import java.io.IOException;
import java.net.SocketAddress;

import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioFactory;

public class NioSmartServer {
	private static NioConfiguration GLOABLECONF;
	protected NioConfiguration nioConf;
	protected NioFactory factory;
	public NioSmartServer(NioConfiguration nioConf,NioFactory factory) {
		this.nioConf = nioConf;
		this.factory = factory;
		GLOABLECONF = this.nioConf;
	}
	
	public void bind() throws IOException {
		factory.getAcceptor(nioConf).bind();
	}
	
	public void bind(SocketAddress address) throws IOException {
		factory.getAcceptor(nioConf).bind(address);
	}
	
	public static NioConfiguration getConfiguration() {
		return GLOABLECONF;
	}
}
