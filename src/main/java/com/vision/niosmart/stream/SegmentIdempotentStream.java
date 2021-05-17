package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.function.Consumer;

import com.vision.niosmart.transport.TransportMonitor;
/**
 * segmentlly idempotent stream, each output segment length base on max segment size is seted
 * @author Jazping
 *
 */
public interface SegmentIdempotentStream extends IdempotentStream{
	/**
	 * return total segments
	 * @return
	 */
	int getSegments();
	/**
	 * return this stream bined connection
	 * @return
	 */
	long getConnectionId();
	
	void setConnectionId(long connectionId);
	/**
	 * return stream max segemnt size
	 * @return
	 */
	int getMaxSegmentSize();
	/**
	 * mark the segment is readed
	 * @param bitIndex to be mark segment index
	 * @return true if not set before this, false if it had seted to true aready
	 */
	boolean set(int bitIndex);
	void set(int fromIndex,int toIndex);
	/**
	 * tell that whether all segment is readed
	 * @return
	 */
	boolean allTrue();
	/**
	 * return true when stream is closed
	 * @return
	 */
	boolean isClosed();
	/**
	 * tell the given segment readed or not
	 * @param bitIndex the segment index
	 * @return
	 */
	boolean get(int bitIndex);
	/**
	 * return this stream remaining segment count
	 * @return
	 */
	int remaining();
	/**
	 * return the reall segment index 
	 * @param segment the whole segment index
	 * @return reall segment index
	 */
	int getSegment(int segment);
	/**
	 * return the reall segment length by given whole index 
	 * @param segment the whole segment index
	 * @return reall segment length
	 * @throws IOException if any IOException occur
	 */
	int getSegmentLength(int segment)throws IOException;
	/**
	 * return reall segement position in the stream
	 * @param segment the whole segment index
	 * @return reall segment position
	 */
	long getPosition(int segment);
	/**
	 * ergodic all idempotent stream
	 * @param c
	 */
	void forEach(Consumer<SegmentIdempotentStream> c);
	
	void setTransportMonitor(TransportMonitor monitor);
	
	TransportMonitor getTransportMonitor();
}
