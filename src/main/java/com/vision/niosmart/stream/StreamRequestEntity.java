package com.vision.niosmart.stream;

public class StreamRequestEntity {
	private long connectionId;
	private long clientId;
	private String name;
	private int stream;
	private int segments;
	private long length;
	private boolean udp;
	
	public StreamRequestEntity() {
		super();
	}
	
	public StreamRequestEntity(long connectionId, String name, int stream, int segments,long length,boolean udp) {
		super();
		this.setConnectionId(connectionId);
		this.name = name;
		this.stream = stream;
		this.segments = segments;
		this.length = length;
		this.udp = udp;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getStream() {
		return stream;
	}
	public void setStream(int stream) {
		this.stream = stream;
	}
	public int getSegments() {
		return segments;
	}
	public void setSegments(int segments) {
		this.segments = segments;
	}

	public long getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(long connectionId) {
		this.connectionId = connectionId;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public boolean isUdp() {
		return udp;
	}

	public void setUdp(boolean udp) {
		this.udp = udp;
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}
}
