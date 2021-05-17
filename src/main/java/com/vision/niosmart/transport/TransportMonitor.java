package com.vision.niosmart.transport;

import java.util.concurrent.atomic.AtomicInteger;


public class TransportMonitor {
	private AtomicInteger counter = new AtomicInteger(0);
	private boolean notified;
	
	public TransportMonitor(int batchSize) {
		if(batchSize<=0) {
			throw new IllegalArgumentException("illegal batch size:"+batchSize);
		}
		counter.set(batchSize);
	}
	
	public void setBathSize(int batchSize) {
		counter.set(batchSize);
	}
	
	public boolean confirm() {
		return counter.decrementAndGet()<=0;
	}
	
	public int getBatchRemaining() {
		return counter.get();
	}
	
	private volatile int moved;
	private AtomicInteger countMoved = new AtomicInteger();
	public void move(int move) {
		if(moved==move) {
			countMoved.incrementAndGet();
		}else {
			countMoved.set(0);
		}
		this.moved = move;
	}
	
	public boolean shouldBeGiveup() {
		return countMoved.get()>20;//2s
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}
}
