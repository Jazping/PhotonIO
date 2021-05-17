package com.vision.niosmart.util;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBuffer;

public class NioBufferUtils {
	private static Logger logger = LoggerFactory.getLogger(NioBufferUtils.class);
//	public static <B> void writeThrow(NioConnection session,DataBuffer dataBuffer,BufferFactory<B> f,Executor executor) {
//		writeThrow(session,dataBuffer,f,executor);
//	}
	
	public static synchronized <B> void writeThrow(NioConnection session,B b,Executor executor) {
		executor.execute(()->session.write(b));
	}
	
	@Deprecated
	public static <B> void writeRetry(NioConnection session,byte[] buffer,BufferFactory<B> f) {
		writeRetry(session,buffer,0,buffer.length,f);
	}
	@Deprecated
	public static <B> void writeRetry(NioConnection session,byte[] buffer,int offset,int length,BufferFactory<B> f) {
		try {
			session.write(f.allocate(buffer,offset,length));
		}catch(OutOfMemoryError e) {
			logger.warn("WAITING FOR BUFFER, {}",e.getMessage());
			session.flush();
			ThreadUtil.wait(f, 100);
			writeRetry(session,buffer,f);
		}
	}
	
	public static <B> void writeRetry(NioConnection session,DataBuffer db,BufferFactory<B> f) {
		try {
			session.write(f.allocate(db.get(),0,db.length()));
		}catch(OutOfMemoryError e) {
			logger.warn("WAITING FOR BUFFER, {}",e.getMessage());
			session.flush();
			ThreadUtil.wait(f, 100);
			writeRetry(session,db,f);
		}
	}
	
	public static <B> void writeRetry(NioConnection session,GByteBuffer db,BufferFactory<B> f) {
		try {
			session.write(db.get());
		}catch(OutOfMemoryError e) {
			logger.warn("WAITING FOR BUFFER, {}",e.getMessage());
			session.flush();
			ThreadUtil.wait(f, 100);
			writeRetry(session,db,f);
		}
	}
}
