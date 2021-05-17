package com.vision.niosmart.nio.buffer;

import com.vision.niosmart.nio.NioConnection;

@Deprecated
public interface CompositeGByteBuffer {
	long join(GByteBuffer g);
	long length();
	void write(NioConnection s);
}
