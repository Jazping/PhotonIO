package com.vision.niosmart.transport;

public class DefaultTransportConf implements TransportConf{
	private int readBuffer;
	private int protocolSize;
	private int receiveBuffer = 1024*1024*8;
	private int sendBuffer = 1024*1024*8;
	private int normalRead = 8*1024;
	private int maxRead = 16*1024;
	
	public DefaultTransportConf(int minRead, int protocolSize, int receiveBuffer,int sendBuffer) {
		super();
		this.readBuffer = minRead;
		this.protocolSize = protocolSize;
		this.receiveBuffer = receiveBuffer;
		this.sendBuffer = sendBuffer;
	}
	
	@Override
	public int getMinRead() {
		return readBuffer;
	}

	@Override
	public int getProtocolSize() {
		return protocolSize;
	}

	@Override
	public int getReceiveBuffer() {
		return receiveBuffer;
	}

	@Override
	public int getSendBuffer() {
		return sendBuffer;
	}

	public void setSendBuffer(int sendBuffer) {
		this.sendBuffer = sendBuffer;
	}

	public int getNormalRead() {
		return normalRead;
	}

	public void setNormalRead(int normalRead) {
		this.normalRead = normalRead;
	}

	public int getMaxRead() {
		return maxRead;
	}

	public void setMaxRead(int maxRead) {
		this.maxRead = maxRead;
	}
	
}
