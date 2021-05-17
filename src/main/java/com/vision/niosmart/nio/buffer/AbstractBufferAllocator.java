package com.vision.niosmart.nio.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.nio.pool.PooledFactory;
/**
 * main implements of the TransportBufferAllocator.
 * @author Jaz
 *
 * @param <B> the shell buffer type
 * @see MinaBufferAllocator
 * @see NettyBufferAllocator
 */
public abstract class AbstractBufferAllocator<A,B> implements TransportBufferAllocator<B>{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private int directSize = 8192;
	private int heapSize = 8192;
	private int largeSize = 64*1024;
	private int tinyDirectSize = 128;
	private int numDirect;
	private int numLarge;
	private int numHeap;
	private int numTinyDirect;
	private PooledFactory<B> directFactory;
	private PooledFactory<B> heapFactory;
	private PooledFactory<B> largetFactory;
	private PooledFactory<B> tinyDirectFactory;
	
	
	protected AbstractBufferAllocator(int directSize, int numDirect,
			int largeSize,int numLarge,
			int heapSize, int numHeap,
			int tinyDirectSize,int numTinyDirect,
			A allocator) {
		if(largeSize<=0) {
			throw new IllegalArgumentException("illegal bufferSize:"+largeSize);
		}
		if(directSize<=0) {
			throw new IllegalArgumentException("illegal bufferSize:"+directSize);
		}
		if(directSize>=largeSize) {
			throw new IllegalArgumentException("directSize must lower largeSize");
		}
		if(heapSize<=0) {
			throw new IllegalArgumentException("illegal bufferSize:"+heapSize);
		}
		if(heapSize>=largeSize) {
			throw new IllegalArgumentException("heapSize must lower largeSize");
		}
		this.directSize = directSize;
		this.largeSize = largeSize;
		this.heapSize = heapSize;
		this.numDirect = numDirect;
		this.numLarge = numLarge;
		this.numHeap = numHeap;
		this.tinyDirectSize = tinyDirectSize;
		this.numTinyDirect = numTinyDirect;
		this.directFactory = directPooledFactory(directSize,numDirect,allocator);
		this.tinyDirectFactory = tinyDirectFactory(tinyDirectSize, numTinyDirect, allocator);
		this.heapFactory = heapPooledFactory(heapSize, numHeap, allocator);
		this.largetFactory = largeHeapPooledFactory(largeSize,numLarge,allocator);
	}
	
	protected abstract PooledFactory<B> directPooledFactory(int directSize, int numDirect,A allocator);
	
	protected abstract PooledFactory<B> tinyDirectFactory(int tinyDirectSize, int numTinyDirect,A allocator);
	
	protected abstract PooledFactory<B> heapPooledFactory(int heapSize, int numHeap,A allocator);
	
	protected abstract PooledFactory<B> largeHeapPooledFactory(int largeSize,int numLarge,A allocator);
	
	private B allocate(boolean direct,int capacity)  {
		if(largeSize<capacity) {
			//no pool
			return unpooledBuffer(direct,capacity);
		}
		if(!direct&&capacity<=largeSize) {
			PooledFactory<B> factory = capacity>heapSize?largetFactory:heapFactory;
			return factory.borrowBuffer();
		}
		if(direct) {
			if(capacity<=tinyDirectSize) {
				return tinyDirectFactory.borrowBuffer();
			}
//			return directFactory.borrowBuffer();
			try {
				return directFactory.borrowBuffer();
			}catch(OutOfMemoryError e) {
				if(directSize>=capacity&&capacity>tinyDirectSize) {
					throw e;//slow down transport
				}
				logger.warn("UNHEALTHY, should be avoid direct buffers using heap pool allocator, size {}",capacity);
				PooledFactory<B> factory = capacity>heapSize?largetFactory:heapFactory;
				return factory.borrowBuffer();
			}
		}
		throw new IllegalStateException("no fitable buffer");
	}
	
	protected abstract B unpooledBuffer(boolean direct,int capacity) ;

	@Override
	public B allocate(int capacity, boolean direct) {
		return allocate(direct,capacity);
	}
	
	@Override
	public void close() {
		this.largetFactory.close();
		this.directFactory.close();
		this.heapFactory.close();
	}
	@Override
	public int activation() {
		return directFactory.activeCount()+largetFactory.activeCount()+heapFactory.activeCount();
	}
	@Override
	public long released() {
		return directFactory.returnedCount()+largetFactory.returnedCount()+heapFactory.returnedCount();
	}
	@Override
	public long created() {
		return directFactory.createdCount()+largetFactory.createdCount()+heapFactory.createdCount();
	}
	@Override
	public int idled() {
		return directFactory.idleCount()+largetFactory.idleCount()+heapFactory.idleCount();
	}
	
	@Override
	public long borrowed() {
		return directFactory.borrowedCount()+largetFactory.borrowedCount()+heapFactory.borrowedCount();
	}

	public int getLargeSize() {
		return largeSize;
	}

	public int getNumLarge() {
		return numLarge;
	}

	public int getDirectSize() {
		return directSize;
	}

	public int getNumDirect() {
		return numDirect;
	}

	public int getHeapSize() {
		return heapSize;
	}

	public int getNumHeap() {
		return numHeap;
	}

	public int getTinyDirectSize() {
		return tinyDirectSize;
	}

	public int getNumTinyDirect() {
		return numTinyDirect;
	}
	
}
