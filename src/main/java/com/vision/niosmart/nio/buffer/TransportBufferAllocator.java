package com.vision.niosmart.nio.buffer;
/**
 * pooled buffer allocator base on commons pool, supported normal size and large size configuration,
 * use to limit momery using, break up io allocator and transport allocator. 
 * if pool active count is limit throw OutOfMemoryError.
 * @author Jazping
 *
 * @param <B> shell buffer type
 * @see AbstractBufferAllocator
 */
public interface TransportBufferAllocator<B> {
	/**
	 * allocate shell buffer with the given capacity
	 * @param capacity
	 * @param direct
	 * @return
	 */
	B allocate(int capacity, boolean direct);
	/**
	 * close the pool
	 */
	void close();
	/**
	 * return buffers that are actived on the pool
	 * @return
	 */
	int activation();
	/**
	 * return total returned count
	 * @return
	 */
	long released();
	/**
	 * return total created count
	 * @return
	 */
	long created();
	/**
	 * return ideld buffer count in currently
	 * @return
	 */
	int idled();
	
	long borrowed();
}
