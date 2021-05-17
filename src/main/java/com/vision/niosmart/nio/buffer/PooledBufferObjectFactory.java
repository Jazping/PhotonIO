package com.vision.niosmart.nio.buffer;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.vision.niosmart.nio.pool.PooledFactory;

public abstract class PooledBufferObjectFactory<B>  extends BasePooledObjectFactory<B>  implements PooledObjectFactory<B>{
	protected PooledFactory<B> pooledFactory;
	protected boolean direct;
	protected int maxSize = 8192;
	
	protected PooledBufferObjectFactory(PooledFactory<B> pooledFactory, boolean direct, int maxSize) {
		super();
		this.pooledFactory = pooledFactory;
		this.direct = direct;
		this.maxSize = maxSize;
	}
	
	@Override
	public B create() throws Exception {
		return newBuffer(pooledFactory,direct,maxSize);
	}
	/**
	 * new unpooled buffer
	 * @param pooledFactory
	 * @param direct
	 * @param capacity
	 * @return
	 */
	protected abstract B newBuffer(PooledFactory<B> pooledFactory,boolean direct,int capacity);

	@Override
	public PooledObject<B> wrap(B obj) {
		return new DefaultPooledObject<>(obj);
	}
}
