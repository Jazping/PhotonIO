package com.vision.niosmart.nio;

import com.vision.niosmart.nio.buffer.BufferFactory;

public abstract class NioAbstractFactory<C,B> {
	protected BufferFactory<B> factory;
	protected NioConnectionFactory<C> pfactory;
	
	protected NioAbstractFactory(BufferFactory<B> factory,NioConnectionFactory<C> pfactory) {
		this.factory = factory;
		this.pfactory = pfactory;
	}

	public BufferFactory<B> getFactory() {
		return factory;
	}

	public void setFactory(BufferFactory<B> factory) {
		this.factory = factory;
	}

	public NioConnectionFactory<C> getPfactory() {
		return pfactory;
	}

	public void setPfactory(NioConnectionFactory<C> pfactory) {
		this.pfactory = pfactory;
	}
}
