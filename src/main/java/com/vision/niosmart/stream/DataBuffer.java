package com.vision.niosmart.stream;

/**
 * pooled byte array object specification, in hightly performance environment, that is required slowly gc activity.
 * if you create byte array frequently, that is impossible to slow down the gc activity.
 * @author Jazping
 *
 */
public interface DataBuffer {
	void release();
	/**
	 * reset and reuse this buffer
	 */
	void reset();
	/**
	 * write a string into buffer
	 * @param string
	 */
	void writeString(String string);
	/**
	 * write write a long into buffer
	 * @param l
	 */
	void writeLong(long l);
	/**
	 * write int into buffer
	 * @param i
	 */
	void writeInt(int i) ;
	/**
	 * write short into buffer
	 * @param s
	 */
	void writeShort(int s);
	/**
	 * write byte array into buffer
	 * @param data
	 */
	void write(byte[] data) ;
	/**
	 * write byte array into buffer
	 * @param data
	 * @param offset
	 * @param length
	 */
	void write(byte[] data,int offset,int length) ;
	/**
	 * get the whole array
	 * @return
	 */
	byte[] get();
	/**
	 * return current used length in whole array
	 * @return
	 */
	int length();
	/**
	 * return total capacity
	 * @return
	 */
	int capacity();
	/**
	 * test the buffer if empty
	 * @return
	 */
	boolean isEmpty();
}