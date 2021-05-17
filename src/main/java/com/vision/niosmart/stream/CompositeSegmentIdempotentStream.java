package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;

import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.transport.TransportMonitor;
/**
 * segment idempotent stream compose, use to fix the small stream
 * @author Admin
 *
 */
public class CompositeSegmentIdempotentStream implements SegmentIdempotentStream {
	private int pointer;
	private List<SegmentIdempotentStream> list;
	private long length;
	private boolean closed;
	private long connectionId;
	private int segments;
	private int remaining;
	private int maxSegmentSize;
	private TransportMonitor monitor;
	
	public CompositeSegmentIdempotentStream(int maxSegmentSize,long connectionId) {
		this.list =  new LinkedList<SegmentIdempotentStream>();
		this.maxSegmentSize = maxSegmentSize;
		this.connectionId = connectionId;
	}
	
	public CompositeSegmentIdempotentStream(SegmentIdempotentStream stream,int maxSegmentSize,long connectionId) throws IOException {
		this(maxSegmentSize,connectionId);
		this.add(stream);
	}
	
	public CompositeSegmentIdempotentStream(Collection<IdempotentStreamWrapper> streams,int maxSegmentSize,long connectionId) throws IOException {
		this(maxSegmentSize,connectionId);
		streams.forEach((s)->this.list.add(s.getStream()));
		this.compLength();
	}
	
	public synchronized long add(SegmentIdempotentStream stream) throws IOException {
		this.list.add(stream);
		return compLength();
	}
	
	private long compLength() throws IOException {
		for(SegmentIdempotentStream stream :list) {
			this.length += stream.length();
			this.segments += stream.getSegments();
			this.remaining += stream.remaining();
		}
		if(current==null) {
			current = list.get(0);
		}
		return this.length;
	}
	
	@Override
	public int getId() {
		return current.getId();
	}

	@Override
	public void setId(int id) {
		current.setId(id);
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public String name() {
		return current.name();
	}
	
	private int off = 0;
	@Override
	public GByteBuffer read(long offset, int length, GByteBuffer destination) throws IOException {
		SegmentIdempotentStream stream = list.get(pointer);
		stream.read(off, length, destination);
		this.off += length;
		return destination;
	}

	@Override
	public synchronized void close() throws IOException {
		list.forEach((s)->IOUtils.closeQuietly(s));
		closed = true;
		list.clear();
	}

	@Override
	public int getSegments() {
		return segments;
	}

	@Override
	public long getConnectionId() {
		return connectionId;
	}

	@Override
	public int getMaxSegmentSize() {
		return maxSegmentSize;
	}

	private int readedSegments;
	private SegmentIdempotentStream current;
	@Override
	public synchronized boolean set(int bitIndex) {
		SegmentIdempotentStream s = list.get(pointer);
		boolean b = s.set(this.getSegment(bitIndex));
		remaining--;
		if(s.allTrue()) {
			pointer++;
			readedSegments+=s.getSegments();
			this.off = 0;
			if(pointer<list.size()) {
				this.current = list.get(pointer);
			}
		}
		return b;
	}

	@Override
	public boolean allTrue() {
		boolean allTrue = true;
		for(SegmentIdempotentStream s : list) {
			if(!s.allTrue()) {
				allTrue = false;
			}
		}
		return allTrue;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public boolean get(int bitIndex) {
		return current.get(this.getSegment(bitIndex));
	}

	@Override
	public int remaining() {
		return remaining;
	}

	public void setConnectionId(long connectionId) {
		this.connectionId = connectionId;
	}

	public void setMaxSegmentSize(int maxSegmentSize) {
		this.maxSegmentSize = maxSegmentSize;
	}

	@Override
	public GByteBuffer readBreak(long offset, int length, GByteBuffer destination) throws IOException {
		throw new BufferException("un supported");
	}

	@Override
	public int getSegment(int segment) {
		if(pointer==0) {
			return segment;
		}
		return segment-readedSegments;
	}

	@Override
	public int getSegmentLength(int segment) throws IOException {
		long len = current.length();
		segment = this.getSegment(segment);
		return Long.valueOf((segment+1)*maxSegmentSize>len?len-maxSegmentSize*segment:maxSegmentSize).intValue();
	}

	@Override
	public long getPosition(int segment) {
		return maxSegmentSize*this.getSegment(segment);
	}
	
	public void forEach(Consumer<SegmentIdempotentStream> c) {
		list.forEach(c);
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
		for(int i=fromIndex;i<toIndex;i++) {
			this.set(i);
		}
	}

}
