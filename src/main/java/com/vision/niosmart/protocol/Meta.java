package com.vision.niosmart.protocol;

public class Meta implements Protocol{
	private long connectionId;
	private int streamId;
	private int segment;
	private long pos;
	private short length;
	public long getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(long connectionId) {
		this.connectionId = connectionId;
	}
	public int getStreamId() {
		return streamId;
	}
	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	public int getSegment() {
		return segment;
	}
	public void setSegment(int segment) {
		this.segment = segment;
	}
	public long getPos() {
		return pos;
	}
	public void setPos(long pos) {
		this.pos = pos;
	}
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
//	@Override
//	public long connectionId() {
//		return this.connectionId;
//	}
	@Override
	public int dataLength() {
		return length;
	}
}
