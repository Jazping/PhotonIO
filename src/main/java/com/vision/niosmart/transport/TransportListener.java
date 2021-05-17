package com.vision.niosmart.transport;

import java.net.InetSocketAddress;

import com.vision.niosmart.stream.Stream;

public interface TransportListener {
	void onFinished(Stream stream);
	void onRepeat(InetSocketAddress destination,long connectionId,int streamId,int segment);
	void connectionNotFound(InetSocketAddress destination,long connectionId);
}
