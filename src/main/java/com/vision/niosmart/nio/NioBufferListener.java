package com.vision.niosmart.nio;

import java.io.IOException;

import com.vision.niosmart.nio.buffer.GByteBuffer;
/**
 * NIO buffer proccessor, proccess buffer one time on each buffer reached,
 * never do too mush work onBuffer method, just split the work.
 * @author Admin
 *
 */
public interface NioBufferListener {
	/**
	 * proccess this buffer one time, never do too mush work onBuffer method, just split the work.
	 * @param id connection id
	 * @param s connection
	 * @param b buffer
	 * @return true if bytes are enough to readed, false if buffer is half of packet
	 * @throws IOException
	 */
	boolean onBuffer(NioConnection connection,GByteBuffer buffer) throws IOException;
}
