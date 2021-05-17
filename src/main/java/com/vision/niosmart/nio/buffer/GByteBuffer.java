package com.vision.niosmart.nio.buffer;

import com.vision.niosmart.nio.NioConnection;

/**
 * Generic buffer method restric
 * @author Jazping
 * @see AbstractGByteBuffer
 */
public interface GByteBuffer {
	/**
	 * read a int value from this buffer
	 * @return int data
	 */
	int readInt();
	/**
	 * read a long value from this buffer
	 * @return long data
	 */
	long readLong();
	
	/**
	 * read a short value from the buffer
	 * @return the short data
	 */
	short readShort();
	
	/**
	 * read a byte array from this buffer
	 * @param dest byte array for read
	 */
	void read(byte[] dest);
	/**
	 * read a byte array from this buffer
	 * @param dest
	 * @param offset
	 * @param length
	 */
	void read(byte[] dest,int offset,int length);
	
	/**
	 * get the remaing of this buffer
	 * @return remaing
	 */
	int remaining();
	
	/**
	 * determin this buffer wether has any more bytes
	 * @param b the buffer
	 * @return true if has, false when not
	 */
	boolean hasRemaining();
	/**
	 * get the pointer of the buffer
	 * @param b the buffer
	 * @return pointer in the buffer
	 */
	int readPosition();
	/**
	 * mark the read pointer in this buffer
	 */
	void markRead();
	/**
	 * set the read pointer in this buffer
	 * @param p point position to set
	 */
	void resetRead();
	/**
	 *  extend the buffer with given bytes, required 0 copy
	 * @param g the generic buffer
	 * @return
	 */
	GByteBuffer extend(GByteBuffer g);
	/**
	 * add buffer object
	 * @param object
	 */
	GByteBuffer add(Object object,boolean clear,boolean first);
	/**
	 * release current buffer
	 */
	void release();
	/**
	 * write int
	 * @param v
	 * @return
	 */
	GByteBuffer writeInt(int v);
	/**
	 * write short
	 * @param v
	 * @return
	 */
	GByteBuffer writeShort(int v);
	/**
	 * write long
	 * @param v
	 * @return
	 */
	GByteBuffer writeLong(long v); 
	/**
	 * write head
	 * @param v
	 * @return
	 */
	GByteBuffer writeString(String v); 
	/**
	 * write bytes
	 * @param b
	 * @param offset
	 * @param length
	 * @return
	 */
	GByteBuffer write(byte[] b,int offset,int length); 
	/**
	 * output to nio connection
	 * @param s
	 */
	void writeTo(NioConnection s);
	
	Object get();
	
	int getBufferCount();
	
	Object pollLast();
	
	Object dropFirst();
	
//	void drop();
}
