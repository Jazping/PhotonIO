package com.vision.niosmart.nio;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.StreamProtocol;

public abstract class NioAbstractHandler<B> implements NioHandler{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected StreamProtocol protocol;
	protected Context context;
	protected BufferFactory<B> factory;
	private Map<Object, GByteBuffer> combine;
	
	
	protected NioAbstractHandler(Context context,StreamProtocol protocol,BufferFactory<B> factory) {
		this.context = context;
		this.protocol = protocol;
		this.factory = factory;
		this.combine = new ConcurrentHashMap<Object, GByteBuffer>(); 
	}

	@Override
	public boolean doRead(NioConnection s, GByteBuffer bf) throws IOException {
		GByteBuffer b = beforeRead(s,bf);
		boolean extend = true;
		if(b==null) {
			b = bf;
			extend = false;
		}
		boolean breakout = false;
		while(b.hasRemaining()) {
			b.markRead();
			if(!protocol.next(s,b)) {
				b.resetRead();
				if(extend&&b.remaining()>dropIfReach(s)) {
					throw new IOException("drop bytes on connection "+s.getId());
				}else{
					breakOut(s,b);
					breakout = true;
					break;
				}
			}
			extend = false;
		}
		if(!bf.equals(b)) {
			b.release();
		}
		return breakout;
	}
	
	protected void breakOut(NioConnection s, GByteBuffer b) {
		if(!b.hasRemaining()) {
			return;
		}
		if(b.remaining()>dropIfReach(s)) {
			throw new BufferException("buffer can not digestion, extend count:"+b.getBufferCount());
		}
		combine.put(s.getNative(), b);
	}
	
	protected GByteBuffer beforeRead(NioConnection s, GByteBuffer b) {
		GByteBuffer g = combine.remove(s.getNative());
		if(g!=null) {
			return b.extend(g);
		}
		return null;
	}
	
	private int dropIfReach(NioConnection s) {
		return s.getAttribute(NioConnection.MAX_SEGMENT_SIZE, 4096)*10;
	}
	
//	private GByteBuffer tryDecrypt(NioConnection s,GByteBuffer bf) {
//		if(s.getAttribute(NioConnection.DEFAULT_PUBLIC_KEY, null)!=null) {
//			
//		}
//		return bf;
//	}
}
