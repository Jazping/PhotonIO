package com.vision.niosmart.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.protocol.Protocol;

public interface UDPDataTransport<E extends Protocol> extends DataTransport<E> {
//	void send(int type,InetSocketAddress destination,long connectionId,int streamId,int segment,byte[]data,int off,int length) throws IOException;
//	void send(ProtocolDatagram datagram,InetSocketAddress destination)throws IOException;
	void send(int type,long connectionId,int streamId,int segment,byte[]data,int off,int length,NioConnection session) throws IOException;
	void removeCache(InetSocketAddress destination);
	int getBatchWait();
	int getReceivePort();
	
//	void flushConfirm(NioConnection tConnection);
	NioConnection getUdpConnection(InetAddress address,int port);
	
}
