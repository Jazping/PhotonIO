package com.vision.niosmart.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.transport.UDPDataTransport;

public interface Connection extends Closeable{
	int UDP_INTERVAL = 100;
	Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	long getId();
	void setClientId(long clientId);
	Long getClientId();
	boolean containStream(int streamId);
	int genStramId();
	void askStream(int streamId,int segments, String name,int segmentSize,long length);
	Stream getStream(int streamId);
	int getRemoteSegSize();
	void setRemoteSegSize(int segmentSize);
	int getRemoteReceivePort();
	void setRemoteReceivePort(int port);
	Stream removeStream(int streamId);
	int getStreamCount();
	void setRemoteReceiveBuf(int buf);
	int getRemoteReceiveBuf();
	
	int getFixBatchSize();
	
	void removeDatagram(int stream);
	int currentDatagramCount();
	SegmentIdempotentStream getDatagramStream(int stream);
	SegmentIdempotentStream putDatagramStream(String key,SegmentIdempotentStream stream);
	SegmentIdempotentStream putDatagramStream(String key,IdempotentStream stream,int segments) throws IOException;
	SegmentIdempotentStream putDatagramStreamUDP(String key,SegmentIdempotentStream stream);
	SegmentIdempotentStream putDatagramStreamUDP(String key,IdempotentStream stream,int segments) throws IOException;
	SegmentIdempotentStream popDatagramStream(boolean isUdp,String key);
	
	void clear();
	void expireStream();
	
	void setUDPTransport(UDPDataTransport<? extends Protocol> transport,InetAddress address,int port);
	NioConnection getUdpConnection(boolean init);
	
	void incBatchSize();
	void decBatchSize();
//	int getInterval();
//	void incInterval();
//	void decInterval();
}
