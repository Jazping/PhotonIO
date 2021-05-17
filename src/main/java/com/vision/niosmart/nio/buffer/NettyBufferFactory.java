package com.vision.niosmart.nio.buffer;

import com.vision.niosmart.exception.BufferException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class NettyBufferFactory implements BufferFactory<ByteBuf>{
	private ByteBufAllocator allocator;
	private int maxLength = 4096;
	
	public NettyBufferFactory(ByteBufAllocator allocator,int maxLength) {
		this.allocator = allocator;
		this.maxLength = maxLength;
	}
	@Override
	public GByteBuffer getGByteBuffer(ByteBuf buf,boolean writting) {
		return new NettyBuffer(buf,writting);
	}

	@Override
	public ByteBuf allocate(byte[] data,int offset,int length) {
		if(length>maxLength) {
			throw new BufferException("buffer length over load:"+(length-maxLength));
		}
		return this.allocator.buffer(length).clear().writeBytes(data,offset,length);
	}
	@Override
	public ByteBuf allocate(int length) {
		return allocator.buffer(length).clear();
	}
	@Override
	public Object getAllocator() {
		return allocator;
	}
}
