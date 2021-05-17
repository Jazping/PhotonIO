package com.vision.niosmart.stream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.vision.niosmart.util.RandomUtils;

class StreamImpl implements Stream {
	private Integer id;
	private int segments;
	private String name;
	
	private ConcurrentBitSet bitSet;
	
	private File tmpFile;
	private AtomicBoolean finished = new AtomicBoolean(false);
	private RandomAccessFile accessFile;
	
	private DataBuffer ioBuffer;
	private int segmentSize;
	private int limit = 1024*512;
	private boolean readed;

	public StreamImpl(Integer id, int segments, String name,int segmentSize,long length,DataBufferFactory factory) {
		super();
		this.id = id;
		this.segments = segments;
		this.name = name;
		this.segmentSize = segmentSize;
		this.bitSet = new ConcurrentBitSet(segments);
		this.ioBuffer = factory.getDataBuffer();
		this.limit = this.ioBuffer.capacity();
	}
	
	@Override
	public Integer getStreamId() {
		return id;
	}

	@Override
	public int getSegments() {
		return segments;
	}

	@Override
	public long getTotalSize() {
		if(!finished.get()) {
			return -1;
		}
		return tmpFile.length();
	}

	@Override
	public synchronized boolean isFully() {
		return bitSet.allTrue();
	}

	@Override
	public synchronized boolean put(int segment,long pos,byte[]data,int offset,int length) throws IOException {
		if(bitSet==null||data==null) {
			throw new NullPointerException();
		}
		if(pos<0) {
			throw new IllegalArgumentException("nagtive pos:"+pos);
		}
		if(bitSet.get(segment)) {
			return false;
		}
		bitSet.set(segment);
		
		ioBuffer.writeLong(pos);
		ioBuffer.writeShort(length);
		ioBuffer.write(data,offset,length);
		if(shouldFlush(ioBuffer)) {
			flush(ioBuffer);
		}
		return true;
	}
	
	private boolean shouldFlush(DataBuffer dataBuffer) {
		return dataBuffer.length()+segmentSize+10>=limit;
	}
	
	private void flush(DataBuffer ioBuffer) throws IOException {
		if(accessFile==null&&shouldFlush(ioBuffer)) {
			try {
				File dir = new File(FileUtils.getTempDirectory(),"niosmart");
				if(!dir.exists()) {
					dir.mkdir();
				}
				tmpFile = new File(dir,RandomUtils.randomBase64UrlSafe("tmp", 8));
				accessFile = new RandomAccessFile(tmpFile, "rw");
			}catch(FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if(accessFile!=null) {
			InputStream inputStream = new ByteArrayInputStream(ioBuffer.get(),0,ioBuffer.length());
			DataInputStream din = new DataInputStream(inputStream);
			while(din.available()>8) {
				long pos = din.readLong();
				int len = din.readShort();
				byte[] data = new byte[len];
				din.read(data);
				accessFile.seek(pos);
				accessFile.write(data, 0, data.length);
			}
			din.close();
			ioBuffer.reset();
		}
	}
	
	@Override
	public synchronized boolean finish() throws IOException {
		if(finished.get()) {
			return true;
		}
		this.flush(this.ioBuffer);
		if(accessFile!=null) {
			accessFile.close();
		}
		finished.set(true);
		return true;
	}

	@Override
	public synchronized InputStream asInputStream() throws IOException{
		if(!finished.get()) {
			throw new IOException("stream not finished");
		}
		if(readed) {
			throw new IOException("stream alread readed");
		}
		readed = true;
		InputStream input = null;
		if(tmpFile!=null) {
			input = new FileInputStream(tmpFile);
		}else {
			InputStream inputStream = new ByteArrayInputStream(ioBuffer.get(), 0, ioBuffer.length());
			DataInputStream din = new DataInputStream(inputStream);
			ByteArrayIdempotentOutputStream out = new ByteArrayIdempotentOutputStream(ioBuffer.length());
			while(din.available()>8) {
				long pos = din.readLong();
				if(pos>=Integer.MAX_VALUE) {
					throw new IOException("too large to caching");
				}
				int len = din.readShort();
				byte[] data = new byte[len];
				din.read(data);
				out.seek(Long.valueOf(pos).intValue());
				out.write(data);
			}
			din.close();
			input = out.asInputStream(0);
		}
		ioBuffer.release();
		return new InputStreamWraper(input,tmpFile); 
	}

	@Override
	public synchronized byte[] asByteArray() throws IOException {
		InputStream inputStream = this.asInputStream();
		try {
			return IOUtils.toByteArray(inputStream);
		}finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void saveTo(OutputStream out) throws IOException{
		this.saveTo(out, 8192);
	}

	@Override
	public synchronized void saveTo(OutputStream out, int readBuffer) throws IOException {
		saveTo(out,new byte[readBuffer]);
	}

	@Override
	public void drop() {
		if(accessFile!=null) {
			IOUtils.closeQuietly(accessFile);
		}
		if(this.tmpFile!=null) {
			this.tmpFile.delete();
		}
		ioBuffer.release();
	}
	
	private class InputStreamWraper extends InputStream{
		private InputStream navtive;
		private File tmpFile;
		
		public InputStreamWraper(InputStream navtive,File tmpFile) {
			super();
			this.navtive = navtive;
			this.tmpFile = tmpFile;
		}

		@Override
		public int read() throws IOException {
			return navtive.read();
		}

		@Override
		public void close() throws IOException {
			navtive.close();
			if(tmpFile!=null) {
				tmpFile.delete();
			}
		}

		@Override
		public int available() throws IOException {
			return navtive.available();
		}

		@Override
		public long skip(long n) throws IOException {
			return navtive.skip(n);
		}

		@Override
		public synchronized void mark(int readlimit) {
			navtive.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			navtive.reset();
		}

		@Override
		public boolean markSupported() {
			return navtive.markSupported();
		}
	}

	@Override
	public synchronized void saveTo(OutputStream out, byte[] buffer) throws IOException {
		InputStream inputStream = this.asInputStream();
		try {
			int count = 0;
			while(count!=-1) {
				count = inputStream.read(buffer);
				if(count!=-1) {
					out.write(buffer,0,count);
				}
			}
			out.flush();
		}finally {
			inputStream.close();
		}
	}

	@Override
	public synchronized void saveTo(OutputStream out, DataBuffer buffer) throws IOException {
		saveTo(out,buffer.get());
	}
}
