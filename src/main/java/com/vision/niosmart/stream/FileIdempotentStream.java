package com.vision.niosmart.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.vision.niosmart.nio.buffer.GByteBuffer;

public class FileIdempotentStream extends AbstractIdempotentStream implements IdempotentStream {
	private RandomAccessFile file;
	private File fil;
	private String name;
	private boolean closed;
	private DataBufferFactory f;
	private boolean deleteOnClose;
	
	public FileIdempotentStream(File file,DataBufferFactory f) throws FileNotFoundException {
		this(file,f,false);
	}
	
	public FileIdempotentStream(File file,DataBufferFactory f,boolean deleteOnClose) throws FileNotFoundException {
		this(0,file,f,deleteOnClose);
	}
	
	public FileIdempotentStream(int id,File file,DataBufferFactory f) throws FileNotFoundException {
		this(id,file,f,false);
	}
	
	public FileIdempotentStream(int id,File file,DataBufferFactory f,boolean deleteOnClose) throws FileNotFoundException {
		super(id);
		this.file = new RandomAccessFile(file,"r");
		this.name = file.getName();
		this.f = f;
		this.fil = file;
		this.deleteOnClose = deleteOnClose;
	}
	
	@Override
	public long length() throws IOException {
		return file.length();
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public synchronized void close() throws IOException {
		file.close();
		closed = true;
		if(deleteOnClose) {
			this.fil.delete();
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public synchronized GByteBuffer read(long offset, int length, GByteBuffer destination) throws IOException {
		if(this.closed) {
			throw new IOException("the stream is closed");
		}
		file.seek(offset);
		DataBuffer buffer = f.getDataBuffer();
		try {
			file.read(buffer.get(), 0, length);
			destination.writeShort(length);
			destination.write(buffer.get(), 0, length);
		}finally {
			buffer.release();
		}
		return destination;
	}
	@Override
	public synchronized GByteBuffer readBreak(long offset, int length, GByteBuffer destination) throws IOException {
		if(!this.closed) {
			throw new IOException("the stream is closed");
		}
		file.seek(offset);
		DataBuffer buffer = f.getDataBuffer();
		try {
			file.read(buffer.get(), 0, length);
			destination.write(buffer.get(), 0, length);
		}finally {
			buffer.release();
		}
		return destination;
	}

}
