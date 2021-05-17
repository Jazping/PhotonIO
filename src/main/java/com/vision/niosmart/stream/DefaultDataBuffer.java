package com.vision.niosmart.stream;

class DefaultDataBuffer implements DataBuffer {
	private byte[]buf;
	private int capacity;
	private int pointer;
	private DataBufferFactory factory;
	private boolean released;
	
	public DefaultDataBuffer(int protocol,int read,DataBufferFactory factory) {
		this(protocol+read,factory);
	}
	
	public DefaultDataBuffer(int capacity,DataBufferFactory factory) {
		this.capacity = capacity;
		buf = new byte[this.capacity];
		this.factory = factory;
	}
	
	@Override
	public synchronized void reset() {
		this.pointer = 0;
		this.released = false;
	}
	
	@Override
	public synchronized void writeString(String string) {
		byte[] data = string.getBytes();
		checkCapacity(data.length+2);
		this.writeShort(data.length);
		System.arraycopy(data, 0, buf, pointer, data.length);
		pointer+=data.length;
	}
	
	@Override
	public synchronized void writeLong(long l) {
		checkCapacity(8);
		buf[pointer++]=(byte)(l >>> 56);
		buf[pointer++]=(byte)(l >>> 48);
		buf[pointer++]=(byte)(l >>> 40);
		buf[pointer++]=(byte)(l >>> 32);
		buf[pointer++]=(byte)(l >>> 24);
		buf[pointer++]=(byte)(l >>> 16);
		buf[pointer++]=(byte)(l >>> 8);
		buf[pointer++]=(byte)(l >>> 0);
	}
	
	@Override
	public synchronized void writeInt(int i) {
		checkCapacity(4);
		buf[pointer++]=(byte)(i >>> 24);
		buf[pointer++]=(byte)(i >>> 16);
		buf[pointer++]=(byte)(i >>> 8);
		buf[pointer++]=(byte)(i >>> 0);
	}
	
	@Override
	public synchronized void writeShort(int s) {
		checkCapacity(2);
		buf[pointer++]=(byte)(s >>> 8);
		buf[pointer++]=(byte)(s >>> 0);
	}
	
	@Override
	public synchronized void write(byte[]data) {
		checkCapacity(data.length);
		System.arraycopy(data, 0, buf, pointer, data.length);
		pointer+=data.length;
	}
	
	@Override
	public synchronized byte[] get() {
		return buf;
	}

	@Override
	public synchronized int length() {
		return pointer;
	}

	@Override
	public synchronized void write(byte[] data, int offset, int length) {
		checkCapacity(length);
		System.arraycopy(data, offset, buf, pointer, length);
		pointer+=length;
	}
	
	private void checkCapacity(int inc) {
		if(released) {
			throw new IllegalStateException("buffer was released");
		}
		if(this.pointer+inc>capacity) {
			throw new ArrayIndexOutOfBoundsException("buffer remaining:"+(capacity-pointer));
		}
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public boolean isEmpty() {
		return length()==0;
	}

	@Override
	public synchronized void release() {
		this.reset();
		if(factory!=null&&!released) {
			factory.release(this);
			released = true;
		}
	}
	
}
