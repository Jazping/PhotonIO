package com.vision.niosmart.protocol;
/**
 * protocol Entity to recvice data segment on NIO, must provide segment length
 * @author Jazping
 *
 */
public interface Protocol {
	/**
	 * return data segment length
	 * @return
	 */
	int dataLength();
}
