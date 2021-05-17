package com.vision.niosmart.consumer;

class LongDecoder implements Decoder {

	@Override
	public Object decode(byte[] data,int offset,int length) throws Exception {
		if(data.length<offset+8) {
			throw new NumberFormatException("data bits < 8");
		}
		int i = offset;
		return (((long)data[i] << 56) +
	                ((long)(data[i+1] & 255) << 48) +
	                ((long)(data[i+2] & 255) << 40) +
	                ((long)(data[i+3] & 255) << 32) +
	                ((long)(data[i+4] & 255) << 24) +
	                ((data[i+5] & 255) << 16) +
	                ((data[i+6] & 255) <<  8) +
	                ((data[i+7] & 255) <<  0));
	}

	@Override
	public boolean isFixLength() {
		return true;
	}

	@Override
	public int getFixLength() {
		return 8;
	}

	@Override
	public Object defaultValue() {
		return 0l;
	}

}
