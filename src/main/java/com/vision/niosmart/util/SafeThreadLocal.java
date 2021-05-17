package com.vision.niosmart.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SafeThreadLocal<T> {
	private Map<Thread, T> container = new ConcurrentHashMap<>();
	
	public void set(T t) {
		container.put(Thread.currentThread(), t);
	}
	
	public T remove() {
		return container.remove(Thread.currentThread());
	}
	
	public T get() {
		return container.get(Thread.currentThread());
	}
}
