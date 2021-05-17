package com.vision.niosmart.consumer;

class BytesDecoder implements Decoder {

	@Override
	public Object decode(byte[] data,int offset,int length) throws Exception {
		if(length==data.length&&offset==0) {
			return data;
		}
		byte[] d = new byte[length];
		System.arraycopy(data, offset, d, 0, length);
		return d;
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
		return new byte[0];
	}

}
