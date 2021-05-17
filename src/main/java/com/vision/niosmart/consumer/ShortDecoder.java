package com.vision.niosmart.consumer;

class ShortDecoder implements Decoder {

	@Override
	public Object decode(byte[] d,int offset,int length) throws Exception {
		if(d.length<offset+2) {
			throw new NumberFormatException("data bits < 2");
		}
		int i = offset;
		if((d[i]<0&&d[i+1]<0)||d[i]==0&&d[i+1]<0) {
			return (short)(((d[i] << 8) + (d[i+1] << 0))+256);
		}
		return (short)((d[i] << 8) + (d[i+1] << 0));
	}

	@Override
	public boolean isFixLength() {
		return true;
	}

	@Override
	public int getFixLength() {
		return 2;
	}

	@Override
	public Object defaultValue() {
		return (short)0;
	}

}
