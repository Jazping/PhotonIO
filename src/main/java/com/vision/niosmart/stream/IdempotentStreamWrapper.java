package com.vision.niosmart.stream;

public class IdempotentStreamWrapper {
	private SegmentIdempotentStream stream;
	private long timestamp;
	
	public IdempotentStreamWrapper(SegmentIdempotentStream stream, long timestamp) {
		super();
		this.stream = stream;
		this.timestamp = timestamp;
	}

	public SegmentIdempotentStream getStream() {
		return stream;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
}
