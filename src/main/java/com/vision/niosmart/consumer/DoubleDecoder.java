package com.vision.niosmart.consumer;

class DoubleDecoder extends LongDecoder implements Decoder {

	@Override
	public Object decode(byte[] data,int offset,int length) throws Exception {
		return Double.longBitsToDouble((long)super.decode(data,offset,length));
	}

}
