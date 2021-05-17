package com.vision.niosmart.consumer;

public interface Decoder {
	Object decode(byte[] data,int offset,int length)throws Exception;
	
	boolean isFixLength();
	
	int getFixLength();
	
	Object defaultValue();
}
