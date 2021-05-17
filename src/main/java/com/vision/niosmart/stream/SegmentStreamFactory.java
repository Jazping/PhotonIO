package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.Collection;

public interface SegmentStreamFactory {
	SegmentIdempotentStream putStream(long connectionId, int streamId, IdempotentStream stream,int segments) throws IOException;
	SegmentIdempotentStream newStream(long connectionId, int streamId, IdempotentStream stream,int segments) throws IOException;
	SegmentIdempotentStream getStream(int streamId);
	void removeStream(int streamId);
	int streamCount();
	Collection<SegmentIdempotentStream> allStreams();
	void clear();
}
