package com.vision.niosmart.nio.pool;

import java.util.NoSuchElementException;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.vision.niosmart.exception.ConnectionException;

public abstract class PooledFactory<T>{
	protected GenericObjectPool<T> objectPool;
	
	protected PooledFactory() {
		
	}
	
	protected PooledFactory(PooledObjectFactory<T> f,int maxTotal) {
		this.init(f, maxTotal);
	}
	
	protected void init(PooledObjectFactory<T> f,int maxTotal) {
		objectPool = new GenericObjectPool<>(f);
		objectPool.setMaxTotal(maxTotal);
		objectPool.setMaxIdle(-1);
		objectPool.setAbandonedConfig(new AbandonedConfig());
	}
	
	public void dropObject(T obj) {
		try {
			objectPool.invalidateObject(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public T borrowBuffer()  {
		if(objectPool.isClosed()) {
			throw new RuntimeException("Pool not open");
		}
		try {
			return objectPool.borrowObject(8000);
		}catch(NoSuchElementException e) { 
			throw new ConnectionException(e.getMessage());
		}catch(ConnectionException e) { 
			throw e;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void release(T g) {
		objectPool.returnObject(g);
	}
	
	public void close() {
		objectPool.close();
	}
	
	public boolean isClosed() {
		return objectPool.isClosed();
	}
	
	public int activeCount() {
		return objectPool.getNumActive();
	}
	
	public long returnedCount() {
		return objectPool.getReturnedCount();
	}
	
	public long createdCount() {
		return objectPool.getCreatedCount();
	}
	
	public long borrowedCount() {
		return objectPool.getBorrowedCount();
	}
	
	public int idleCount() {
		return objectPool.getNumIdle();
	}
}
