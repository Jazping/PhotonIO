package com.vision.niosmart.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadUtil {
	public static void sleep(int ms) {
		if(ms<0)throw new IllegalArgumentException("navigate million seconds");
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void sleep(Object target,int ms) {
		notNull(target);
		synchronized (target) {
			sleep(ms);
		}
	}
	
	public static void wait(Object target,int ms) {
		notNull(target);
		synchronized (target) {
			try {
				if(ms<0) {
					target.wait();
				}else {
					target.wait(ms);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void notNull(Object target) {
		if(target==null) {
			throw new NullPointerException();
		}
	}
	
	public static void notifyAll(Object target) {
		notNull(target);
		synchronized (target) {
			target.notifyAll();
		}
	}
	
	public static void notify(Object target) {
		if(target!=null) {
			synchronized (target) {
				target.notify();
			}
		}
	}
	
	/**
	 * batch submit task and awaiting result set
	 * @param tasks 
	 * @param executor
	 * @param c
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static final <T,R> List<R> submits(T[] tasks,ExecutorService executor,Function<T,R> applier) throws InterruptedException, ExecutionException{
    	if(tasks==null) {
    		return Collections.emptyList();
    	}
    	List<Future<R>> list = new LinkedList<>();
    	for(T t : tasks){
    		list.add(submit(executor,()->applier.apply(t)));
    	}
    	return collect(list);
    }
	/**
	 * batch submit task and awaiting result set
	 * @param iterable
	 * @param executor
	 * @param applier
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static final <T,R> List<R> submits(Iterable<T> tasks,ExecutorService executor,Function<T,R> applier) throws InterruptedException, ExecutionException{
    	if(tasks==null) {
    		return Collections.emptyList();
    	}
    	List<Future<R>> list = new LinkedList<>();
    	tasks.forEach((t)->list.add(submit(executor,()->applier.apply(t))));
    	return collect(list);
    }
	
	/**
	 * submit task and return Future
	 * @param executor
	 * @param c
	 * @return
	 */
	public static final <T> Future<T> submit(ExecutorService executor,Supplier<T> c) {
		return executor==null?null:executor.submit(new Callable<T>() {
				@Override
				public T call() throws Exception {
					return c.get();
			}});
	}
	
	/**
	 * collecting results in Future list
	 * @param fs
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
    public static final <T> List<T> collect(List<Future<T>> fs) throws InterruptedException, ExecutionException{
    	List<T> listInfo = new ArrayList<>(fs.size());
    	for(Future<T> f : fs) {
    		listInfo.add(f!=null?f.get():null);
    	}
    	return listInfo;
    }
    
}
