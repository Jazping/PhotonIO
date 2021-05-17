package com.vision.niosmart.client;

import org.apache.commons.pool2.PooledObjectFactory;

import com.vision.niosmart.nio.pool.PooledFactory;

/**
 * NioSmart client pool, use to pressure test
 * @author Jazping
 *
 * @param <B> buffer type
 */
public class ClientPool<B> extends PooledFactory<BaseNioSmartClient<B>>{
	
	public ClientPool(PooledObjectFactory<BaseNioSmartClient<B>> f, int maxTotal) {
		super(f, maxTotal);
	}

	@Override
	public BaseNioSmartClient<B> borrowBuffer() {
		BaseNioSmartClient<B> client = super.borrowBuffer();
		return client;
	}
}
