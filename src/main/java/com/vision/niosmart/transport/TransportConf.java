package com.vision.niosmart.transport;

/**
 * nio smart transport configuration, config the socket generic parameters
 * @author Jazping
 *
 */
public interface TransportConf {
	/**
	 * return socket reading buffer
	 * @return
	 */
	int getMinRead();
	/**
	 * indicate transport protocol length
	 * @return
	 */
	int getProtocolSize();
	/**
	 * return socket recive buffer
	 * @return
	 */
	int getReceiveBuffer();
	/**
	 * return socket sending buffer
	 * @return
	 */
	int getSendBuffer();
	/**
	 * return normal read buffer on each reading
	 * @return
	 */
	int getNormalRead();
	/**
	 * return max read buffer on each reading
	 * @return
	 */
	int getMaxRead();
}
