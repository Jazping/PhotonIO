package com.vision.niosmart.stream;

import java.io.Closeable;
import java.io.IOException;

import com.vision.niosmart.nio.buffer.GByteBuffer;
/**
 * Idempotented stream for protocol base transport, read method for output to gbuffer 
 * @author Jazping
 *
 */
public interface IdempotentStream extends Closeable{
	/**
	 * return this stream's id
	 * @return
	 */
	int getId();
	/**
	 * set this stream's id
	 * @param id
	 */
	void setId(int id);
	/**
	 * return this stream length
	 * @return
	 * @throws IOException
	 */
	long length() throws IOException;
	/**
	 * return stream name
	 * @return
	 */
	String name();
	/**
	 * output to gbuffer
	 * @param offset stream offset
	 * @param length lenth of this output
	 * @param destination target for revcie bytes
	 * @return the target gbuffer
	 * @throws IOException if any io exception occur
	 */
	GByteBuffer read(long offset,int length,GByteBuffer destination) throws IOException;
	/**
	 * output to gbuffer without data length mark, use to output breaked data
	 * @param offset stream offset
	 * @param length lenth of this output
	 * @param destination target for revcie bytes
	 * @return the target gbuffer
	 * @throws IOException if any io exception occur
	 */
	GByteBuffer readBreak(long offset,int length,GByteBuffer destination) throws IOException;
}
