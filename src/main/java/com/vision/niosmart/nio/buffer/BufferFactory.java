package com.vision.niosmart.nio.buffer;
/**
 * Generic buffer factory, use to admin the generic buffer with pool, and alloate new buffer indicate by the Type Parameters.
 * buffer allocate should be limit to avoid tcp traffic 
 * @author Jazping
 *
 * @param <B> buffer type
 */
public interface BufferFactory<B> {
	/**
	 * get generic buffer from the object pool
	 * @param buf buffer type
	 * @param writting true if for write, false if reading
	 * @return generic buffer
	 */
	GByteBuffer getGByteBuffer(B buf,boolean writting);
	/**
	 * allocate buffer for comunicate
	 * @param data src
	 * @param offset pos
	 * @param length length
	 * @return
	 */
	B allocate(byte[] data,int offset,int length);
	/**
	 * allocate buffer for transport
	 * @param length
	 * @return
	 */
	B allocate(int length);
	
	Object getAllocator();
}
