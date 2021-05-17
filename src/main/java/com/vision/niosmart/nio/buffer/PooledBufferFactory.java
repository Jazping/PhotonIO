package com.vision.niosmart.nio.buffer;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.vision.niosmart.nio.pool.PooledFactory;

public class PooledBufferFactory<B> extends PooledFactory<B>{
	
	protected PooledBufferFactory(int maxTotal,int maxBufferSize,PooledBufferObjectFactory<B> pooledObjectFactory,boolean direct) {
		objectPool = new GenericObjectPool<>(pooledObjectFactory);
		objectPool.setMaxTotal(-1);
		objectPool.setMaxIdle(-1);
		objectPool.setAbandonedConfig(new AbandonedConfig());
	}
}
