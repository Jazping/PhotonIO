package com.vision.niosmart.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Stream {
	/**
	 * return stream id
	 * @return
	 */
	Integer getStreamId();
	/**
	 * return the stream segment number
	 * @return
	 */
	int getSegments();
	/**
	 * return the stream length
	 * @return
	 */
	long getTotalSize();
	/**
	 * return true if this stream is all segment reviced
	 * @return
	 */
	boolean isFully();
	/**
	 * revcie one segment
	 * @param segment
	 * @param pos
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	boolean put(int segment,long pos,byte[]data,int offset,int length)throws IOException;
	/**
	 * finish data revcice
	 * @return
	 * @throws IOException
	 */
	boolean finish() throws IOException;
	/**
	 * as input stream return, if that are readed before, it will throw IOException
	 * @return
	 * @throws IOException
	 */
	InputStream asInputStream() throws IOException;
	/**
	 * read to a byte array, if that are readed before, it will throw IOException
	 * @return
	 * @throws IOException
	 */
	byte[] asByteArray()throws IOException;
	/**
	 * get the stream name
	 * @return
	 */
	String getName();
	/**
	 * save to the output stream but not close output stream
	 * @param out
	 * @throws IOException
	 */
	void saveTo(OutputStream out)throws IOException;
	/**
	 * save to the output stream but not close output stream
	 * @param out
	 * @param readBuffer
	 * @throws IOException
	 */
	void saveTo(OutputStream out,int readBuffer)throws IOException;
	/**
	 * save to the output stream but not close output stream
	 * @param out
	 * @param buffer
	 * @throws IOException
	 */
	void saveTo(OutputStream out,byte[] buffer)throws IOException;
	/**
	 * save to the output stream but not close output stream
	 * @param out
	 * @param buffer
	 * @throws IOException
	 */
	void saveTo(OutputStream out,DataBuffer buffer)throws IOException;
	/**
	 * drop in exception
	 */
	void drop();
}
