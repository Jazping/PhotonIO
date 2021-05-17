package com.vision.niosmart;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.Stream;

public interface Context {
	long applyForConnectionId(long connectionId)throws ConnectionException;
	void closeConnection(long connectionId);
	int connectionCount();
	Connection getConnection(long connectionId);
	boolean isConnectionAvailable(long connectionId);
	void askStream(long connectionId,int streamId,int segments, String name,int segmentSize,long length)throws StreamException;
	Stream deleteStream(long connectionId,int streamId);
	int currentStreamCount(long connectionId);
	boolean isStreamAvailable(long connectionId,int streamId);
	boolean isStreamFully(long connectionId,int streamId);
	boolean finishStream(long connectionId,int streamId)throws IOException;
	boolean fillStream(long connectionId,int streamId,int segment,long pos,byte[]data,int offset,int length)throws StreamException;
	Stream getStream(long connectionId,int streamId);
	String getCharset();
	
	SegmentIdempotentStream getDatagramStream(long id,int stream);
	SegmentIdempotentStream putDatagramStream(long id, String key, IdempotentStream stream,int segmentSize)throws IOException;
	SegmentIdempotentStream putDatagramStreamUDP(long id, String key, IdempotentStream stream,int segmentSize)throws IOException;
	SegmentIdempotentStream popDatagramStream(long id,boolean isUdp,String key);
	
	void removeDatagram(long id,SegmentIdempotentStream stream) ;
	int currentDatagramCount(long connectionId);
	
	void putGetting(String name);
	boolean removeGetting(String name);
	boolean hasGetting();
	void clearGetting();
	
	void putAddress(InetSocketAddress address);
	void removeAddress(InetSocketAddress address);
	boolean isAvailable(InetSocketAddress address);
	
//	int getNetwideLimit();
}
