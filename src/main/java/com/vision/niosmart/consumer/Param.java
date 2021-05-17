package com.vision.niosmart.consumer;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * parameter's annotation in consumer method
 * 
 * @author Jazping
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
	/**
	 * parameter length, negative present the length indicated by other parameter
	 * @return
	 */
	int value() default 0;
	/**
	 * parameter resolving will break if one argument preset this mark, 
	 * and then fill the default value to latter parameters in the consumer method.
	 * 
	 * @return
	 */
	String truncate() default "";
	/**
	 * custom defined decoder for specified argument
	 * @return
	 */
	Class<? extends Decoder> codec() default Decoder.class;
}
