package com.vision.niosmart.consumer;

class FloatDecoder extends IntDecoder implements Decoder {

	@Override
	public Object decode(byte[] d,int offset,int length) throws Exception {
		return Float.intBitsToFloat((int)super.decode(d,offset,length));
	}

}
