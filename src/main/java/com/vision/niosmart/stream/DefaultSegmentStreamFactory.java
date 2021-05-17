package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;


public class DefaultSegmentStreamFactory implements SegmentStreamFactory{
	
	private Map<Integer, SegmentIdempotentStream> map = new ConcurrentHashMap<Integer, SegmentIdempotentStream>();
	
	@Override
	public SegmentIdempotentStream putStream(long connectionId, int streamId,IdempotentStream stream,int segments) throws IOException {
		SegmentIdempotentStream datagram = newStream(connectionId,streamId,stream,segments);
		map.put(streamId, datagram);
		return datagram;
	}
	
	@Override
	public SegmentIdempotentStream newStream(long connectionId, int streamId,IdempotentStream stream,int segmentSize) throws IOException {
		return new BitsetSegmentIdempotentStream(connectionId,streamId,stream,segmentSize);
	}
	
	@Override
	public SegmentIdempotentStream getStream(int streamId) {
		return map.get(streamId);
	}
	
	@Override
	public void removeStream(int streamId) {
		map.remove(streamId);
	}
	
	@Override
	public int streamCount() {
		return map.size();
	}
	
	@Override
	public Collection<SegmentIdempotentStream> allStreams(){
		return map.values();
	}

	@Override
	public void clear() {
		this.map.forEach((k,v)->IOUtils.closeQuietly(v));
		this.map.clear();
	}
	
	
	
}
