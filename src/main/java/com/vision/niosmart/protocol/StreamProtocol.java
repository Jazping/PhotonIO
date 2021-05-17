package com.vision.niosmart.protocol;

import java.io.IOException;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.GByteBuffer;
/**
 * stream protocol to read nio data, according to the protocol, that is not allow throw like buffer under flow exception,
 * if so, that shoud be return false.
 * @author Jazping
 *
 */
public interface StreamProtocol {
	/**
	 * next segment to do
	 * @param s connection delegate
	 * @param b buffer delegate
	 * @return true if done, false if underflow or out of bounder
	 * @throws Exception any exception to throw
	 */
	boolean next(NioConnection s,GByteBuffer b)throws IOException;
}
