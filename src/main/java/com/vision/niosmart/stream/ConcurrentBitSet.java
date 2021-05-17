package com.vision.niosmart.stream;

import java.util.BitSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ConcurrentBitSet {

	/**
	 * 
	 */
	
	private BitSet bitSet;
	private int length;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	public ConcurrentBitSet(int length) {
		this.bitSet = new BitSet(length);
		this.length = length;
	}
	
	public boolean get(int index) {
		lock.readLock().lock();
		try {
			return this.bitSet.get(index);
		}finally {
			lock.readLock().unlock();
		}
	}
//	
//	public BitSet get(int from,int toIndex) {
//		lock.readLock().lock();
//		try {
//			return this.bitSet.get(from, toIndex);
//		}finally {
//			lock.readLock().unlock();
//		}
//	}
	
//	 public void set(int fromIndex, int toIndex, boolean value) {
//		 lock.writeLock().lock();
//		 try {
//			 this.bitSet.set(fromIndex, toIndex, value);
//		 }finally {
//			 lock.writeLock().unlock();
//		 }
//	 }
	 
	 public void set(int fromIndex, int toIndex) {
		 lock.writeLock().lock();
		 try {
			 this.bitSet.set(fromIndex, toIndex);
		 }finally {
			 lock.writeLock().unlock();
		 }
	 }
	 
//	 public void set(int bitIndex, boolean value) {
//		 lock.writeLock().lock();
//		 try {
//			 this.bitSet.set(bitIndex, value);
//		 }finally {
//			 lock.writeLock().unlock();
//		 }
//	 }
	 
	 public boolean set(int bitIndex) {
		 lock.writeLock().lock();
		 try {
			 boolean b = this.bitSet.get(bitIndex);
			 this.bitSet.set(bitIndex);
			 return !b;
		 }finally {
			 lock.writeLock().unlock();
		 }
	 }
	 
	 public boolean allTrue() {
		 return remaining()==0;
	 }
	 
	 public int remaining() {
		 lock.readLock().lock();
		 try {
			 return this.length - this.bitSet.cardinality();
		 }finally {
			 lock.readLock().unlock();
		 }
	 }
	 
//	 private int cardinality() {
//		 lock.readLock().lock();
//		 try {
//			 return this.bitSet.cardinality();
//		 }finally {
//			 lock.readLock().unlock();
//		 }
//	 }

	@Override
	public String toString() {
		return "total:"+this.length+", used:"+this.bitSet.cardinality();
	}
	
	

}
