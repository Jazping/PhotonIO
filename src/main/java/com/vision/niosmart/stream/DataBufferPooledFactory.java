package com.vision.niosmart.stream;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

class DataBufferPooledFactory extends BasePooledObjectFactory<DataBuffer>{
	private int protocol;
	private int read;
	private DataBufferFactory factory;
	public DataBufferPooledFactory(int protocol,int read,DataBufferFactory factory) {
		this.protocol = protocol;
		this.read = read;
		this.factory = factory;
	}
	
	@Override
	public DataBuffer create() throws Exception {
		return new DefaultDataBuffer(protocol,read,factory);
	}

	@Override
	public PooledObject<DataBuffer> wrap(DataBuffer obj) {
		return new DefaultPooledObject<>(obj);
	}

}

