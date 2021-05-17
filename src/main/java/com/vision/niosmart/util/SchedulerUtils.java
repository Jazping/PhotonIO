package com.vision.niosmart.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vision.niosmart.transport.NotifiedSchedulerExecutor;

public class SchedulerUtils {
	public static NotifiedSchedulerExecutor newScheduler(int numThread,boolean deam) {
		return new NotifyableScheduler(new ScheduledThreadPoolExecutor(numThread, new ThreadFactory() {
			private volatile int counter = 1; 
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r,"Scheduler-"+(counter++));
				t.setDaemon(deam);
				return t;
			}}));
	}
	
	private static class NotifyableScheduler implements NotifiedSchedulerExecutor{
		private ScheduledExecutorService nativeExecutor;
		public NotifyableScheduler(ScheduledExecutorService nativeExecutor) {
			this.nativeExecutor = nativeExecutor;
		}
		@Override
		public void schedule(Runnable command,Supplier<Boolean> condition,int delay) {
			nativeExecutor.schedule(()->{
				if(condition.get()) {
					command.run();
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
		@Override
		public void schedule(Runnable command, Supplier<Boolean> condition,int delay,Supplier<Boolean> s) {
			nativeExecutor.schedule(()->{
				if(s.get()&&condition.get()) {
					command.run();
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
		@Override
		public void schedule(Runnable command, Supplier<Boolean> condition, int delay, Consumer<Boolean> s) {
			nativeExecutor.schedule(()->{
				if(condition.get()) {
					command.run();
				}else {
					s.accept(null);
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	}
}
