package com.vision.niosmart.consumer;

class IntDecoder implements Decoder {

	@Override
	public Object decode(byte[] d,int offset,int length) throws Exception {
		if(d.length<offset+4) {
			throw new NumberFormatException("data bits < 4");
		}
		int i = offset;
		return ((d[i] << 24) + (d[i+1] << 16) + (d[i+2] << 8) + (d[i+3] << 0));
	}

	@Override
	public boolean isFixLength() {
		return true;
	}

	@Override
	public int getFixLength() {
		return 4;
	}

	@Override
	public Object defaultValue() {
		return 0;
	}

}
