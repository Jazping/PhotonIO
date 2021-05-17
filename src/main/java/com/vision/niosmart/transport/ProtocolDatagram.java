package com.vision.niosmart.transport;

public class ProtocolDatagram {
	private int type;
//	private InetAddress to;
//	private int port;
	private long connectionId;
	private int streamId;
	private int segment;
	private byte[]data;
	private int off;
	private int length;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
//	public InetAddress getTo() {
//		return to;
//	}
//	public void setTo(InetAddress to) {
//		this.to = to;
//	}
//	public int getPort() {
//		return port;
//	}
//	public void setPort(int port) {
//		this.port = port;
//	}
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
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getOff() {
		return off;
	}
	public void setOff(int off) {
		this.off = off;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}
