package com.vision.niosmart.nio;

import com.vision.niosmart.nio.buffer.GByteBuffer;

public interface NioHandler {
	/**
	 * do read with nio connection and generic buffer
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	boolean doRead(NioConnection ctx, GByteBuffer msg) throws Exception;
}
