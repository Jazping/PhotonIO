package com.vision.niosmart.stream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * an idempotent output stream supported stream's modification when outputting before flush, 
 * and these modification are comply idempotent way.
 * @author Jazping
 *
 */
public abstract class IdempotentOutputStream extends OutputStream{
	/**
	 * seek to the position before writing
	 * @param pos
	 */
	abstract void seek(int pos);
	/**
	 * as input stream returning after wrote
	 * @param pos
	 * @return
	 */
	abstract InputStream asInputStream(int pos);
}
