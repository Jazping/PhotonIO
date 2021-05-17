package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.function.Consumer;

import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.transport.TransportMonitor;

class BitsetSegmentIdempotentStream implements SegmentIdempotentStream{
	private int id;
	private int segments;
	private long connectionId;
	private ConcurrentBitSet bitSet;
	private IdempotentStream stream;
	private int maxSegmentSize;
	private boolean closed;
	private TransportMonitor monitor;
	public BitsetSegmentIdempotentStream(long connectionId,int id, IdempotentStream stream, int maxSegmentSize) throws IOException {
		super();
		this.id = id;
		this.stream = stream;
		this.maxSegmentSize = maxSegmentSize;
		long avilable = stream.length();
		if(avilable==0) {
			throw new IOException("empty data");
		}
		long segments = avilable<=maxSegmentSize?1:avilable/maxSegmentSize;
		if(avilable>maxSegmentSize&&avilable%maxSegmentSize!=0) {
			segments++;
		}
		this.setSegments(Long.valueOf(segments).intValue());
		this.setConnectionId(connectionId);
	}
	
	public BitsetSegmentIdempotentStream() {
		super();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSegments() {
		return segments;
	}
	public void setSegments(int segments) {
		this.segments = segments;
		this.bitSet = new ConcurrentBitSet(segments);
	}
	public long getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(long connectionId) {
		this.connectionId = connectionId;
	}
	public ConcurrentBitSet getBitSet() {
		return this.bitSet;
	}
	public int getMaxSegmentSize() {
		return maxSegmentSize;
	}
	public long length() throws IOException {
		return this.stream.length();
	}
	
	public boolean isClosed() {
		return this.closed;
	}

	@Override
	public void close() throws IOException {
		if(!closed) {
			stream.close();
			closed = true;
		}
	}

	@Override
	public String name() {
		return stream.name();
	}

	@Override
	public boolean set(int bitIndex) {
		return this.bitSet.set(bitIndex);
	}

	@Override
	public boolean allTrue() {
		return bitSet.allTrue();
	}

	@Override
	public boolean get(int bitIndex) {
		return bitSet.get(bitIndex);
	}

	@Override
	public int remaining() {
		return bitSet.remaining();
	}

	@Override
	public GByteBuffer read(long offset, int length, GByteBuffer destination) throws IOException {
		return this.stream.read(offset, length, destination);
	}

	@Override
	public GByteBuffer readBreak(long offset, int length, GByteBuffer destination) throws IOException {
		return this.stream.readBreak(offset, length, destination);
	}

	@Override
	public int getSegment(int segment) {
		return segment;
	}

	@Override
	public int getSegmentLength(int segment) throws IOException {
		long len = this.length();
		return Long.valueOf((segment+1)*maxSegmentSize>len?len-maxSegmentSize*segment:maxSegmentSize).intValue();
	}

	@Override
	public long getPosition(int segment) {
		return maxSegmentSize*segment;
	}

	@Override
	public void forEach(Consumer<SegmentIdempotentStream> c) {
		c.accept(this);
	}

	@Override
	public void setTransportMonitor(TransportMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public TransportMonitor getTransportMonitor() {
		return monitor;
	}

	@Override
	public void set(int fromIndex, int toIndex) {
		bitSet.set(fromIndex, toIndex);
	}
	
}
