package com.vision.niosmart.stream;

import java.io.IOException;

import com.vision.niosmart.nio.buffer.GByteBuffer;

public class ByteIdempotentStream extends AbstractIdempotentStream implements IdempotentStream {
	private byte[] data;
	private String name;
	
	public ByteIdempotentStream(int id,byte[] data,String name) {
		super(id);
		this.data = data;
		this.name = name;
	}
	@Override
	public long length() throws IOException {
		return data.length;
	}

	@Override
	public String name() {
		return this.name;
	}
	@Override
	public void close() throws IOException {
		
	}
	@Override
	public GByteBuffer read(long offset, int length, GByteBuffer destination) throws IOException {
		destination.writeShort(length);
		destination.write(data,Long.valueOf(offset).intValue(),length);
		return destination;
	}
	@Override
	public GByteBuffer readBreak(long offset, int length, GByteBuffer destination) throws IOException {
		destination.write(data,Long.valueOf(offset).intValue(),length);
		return destination;
	}

}
