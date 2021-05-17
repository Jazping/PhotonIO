package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.vision.niosmart.nio.pool.PooledFactory;

public class DefaultDataBufferFactory extends PooledFactory<DataBuffer> implements DataBufferFactory {

	public DefaultDataBufferFactory(int protocol,int size,int maxTotal) {
		DataBufferPooledFactory p = new DataBufferPooledFactory(protocol,size,this);
		super.init(p, maxTotal);
	}
	
	public DefaultDataBufferFactory(int size,int maxTotal) {
		DataBufferPooledFactory p = new DataBufferPooledFactory(0,size,this);
		super.init(p, maxTotal);
	}

	@Override
	public DataBuffer getDataBuffer() {
		DataBuffer dataBuffer = super.borrowBuffer();
		dataBuffer.reset();
		return dataBuffer;
	}

	@Override
	public void release(DataBuffer dataBuffer) {
		super.release(dataBuffer);
	}

	@Override
	public <R> R functionThrowIOE(IOExceptionFunction<DataBuffer, R> c) throws IOException{
		DataBuffer buffer = this.getDataBuffer();
		try {
			return c.apply(buffer);
		}finally {
			this.release(buffer);
		}
	}

	@Override
	public void consumThrowIOE(IOExceptionConsumer<DataBuffer> c) throws IOException{
		DataBuffer buffer = this.getDataBuffer();
		try {
			c.accept(buffer);
		}finally {
			this.release(buffer);
		}
	}
	
	@Override
	public void consum(Consumer<DataBuffer> c){
		DataBuffer buffer = this.getDataBuffer();
		try {
			c.accept(buffer);
		}finally {
			this.release(buffer);
		}
	}

	@Override
	public <R> R function(Function<DataBuffer, R> c) {
		DataBuffer buffer = this.getDataBuffer();
		try {
			return c.apply(buffer);
		}finally {
			this.release(buffer);
		}
	}

}
