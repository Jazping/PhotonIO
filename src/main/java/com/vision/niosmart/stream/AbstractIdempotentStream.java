package com.vision.niosmart.stream;

public abstract class AbstractIdempotentStream implements IdempotentStream {
	private int id;
	protected AbstractIdempotentStream(int id) {
		this.id = id;
	}
	@Override
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
