package com.vision.niosmart.consumer;

class StringDecoder implements Decoder {

	@Override
	public Object decode(byte[] data,int offset,int length) throws Exception {
		return new String(data,offset,length);
	}

	@Override
	public boolean isFixLength() {
		return false;
	}

	@Override
	public int getFixLength() {
		return -1;
	}

	@Override
	public Object defaultValue() {
		return "";
	}

}
