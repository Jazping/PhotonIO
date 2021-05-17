package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * pooled databuffer factory, provide atomic using buffer
 * @author Jaz.
 *
 */
public interface DataBufferFactory {
	
	DataBuffer getDataBuffer();
	
	void release(DataBuffer dataBuffer);
	/**
	 * atomic using buffer and release, required synchorinzed invoking
	 * @param <R>
	 * @param c
	 * @return
	 */
	<R> R functionThrowIOE(IOExceptionFunction<DataBuffer,R> c)throws IOException;
	/**
	 * atomic using buffer and release, required synchorinzed invoking
	 * @param <R>
	 * @param c
	 * @return
	 */
	<R> R function(Function<DataBuffer,R> c);
	/**
	 * atomic using buffer and release, required synchorinzed invoking
	 * @param c
	 */
	void consumThrowIOE(IOExceptionConsumer<DataBuffer> c)throws IOException;
	/**
	 * atomic using buffer and release, required synchorinzed invoking
	 * @param c
	 */
	void consum(Consumer<DataBuffer> c);
}
