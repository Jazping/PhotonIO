package com.vision.niosmart.transport;

import java.util.function.Consumer;
import java.util.function.Supplier;
/**
 * provide delay executor task by given condition, so you can blocking in condition and begining
 * <b>
 * <pre>
 * scheduler.schedule(new Runable(){
 * 	//you can blocking here
 * 	targetRunable.run();//if time out or notified, run it directly
 * },250);
 * <pre>
 * @author Jazping
 *
 */
public interface NotifiedSchedulerExecutor {
	/**
	 * schedule command with given consumer to do private blocking
	 * @param command command to run
	 * @param condition notifiable blocking code(recommend: reall time - delay)
	 * @param delay delay milliseconds at begining
	 */
	void schedule(Runnable command,Supplier<Boolean> condition,int delay,Supplier<Boolean> s);
	void schedule(Runnable command,Supplier<Boolean> condition,int delay,Consumer<Boolean> s);
	/**
	 * schedule command with given supplier to do private blocking
	 * @param command command to run
	 * @param condition notifiable blocking code(recommend: reall time - delay)
	 * @param delay delay milliseconds at begining
	 */
	void schedule(Runnable command,Supplier<Boolean> condition,int delay);
}
