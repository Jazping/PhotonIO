package com.vision.niosmart.stream;

public interface StreamFactory {
	Stream newStream(Integer id, int segments, String name,int segmentSize,long length);
	void clear();
}
