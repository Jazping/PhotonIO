package com.vision.niosmart.consumer;

class ByteDecoder implements Decoder {

	@Override
	public Object decode(byte[] data,int offset,int length) throws Exception {
		if(data.length<1) {
			throw new NumberFormatException("data bits < 1");
		}
		return (byte)(data[offset]);
	}

	@Override
	public boolean isFixLength() {
		return true;
	}

	@Override
	public int getFixLength() {
		return 1;
	}

	@Override
	public Object defaultValue() {
		return (byte)0;
	}


}
