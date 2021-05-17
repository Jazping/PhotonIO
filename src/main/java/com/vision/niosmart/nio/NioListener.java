package com.vision.niosmart.nio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBuffer;
import com.vision.niosmart.stream.DataBufferFactory;

/**
 * General NIO Stream Listener, never do too mush work onBuffer method, just split the work
 * @author Jazping
 *
 * @param <S> the connection type
 */
public abstract class NioListener implements NioBufferListener{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected Context context;
	protected boolean checkId;
	protected DataBufferFactory dbf;
	
	protected NioListener() {
		
	}
	
	protected NioListener(DataBufferFactory dbf) {
		this(dbf, null);
	}
	
	protected NioListener(DataBufferFactory dbf,boolean checkId) {
		this(dbf,null, checkId);
	}
	/**
	 * construct a uncheck listener
	 * @param context Context
	 */
	protected NioListener(DataBufferFactory dbf,Context context) {
		this(dbf,context, false);
	}
	
	/**
	 * construct listener
	 * @param context Context
	 * @param checkId check or not
	 */
	protected NioListener(DataBufferFactory dbf,Context context,boolean checkId) {
		super();
		this.dbf = dbf;
		this.context = context;
		this.checkId = checkId;
	}
	
	@Override
	public boolean onBuffer(NioConnection s, GByteBuffer b) throws IOException {
		long id = s.getId();
		return onBuffer(id,s,b);
	}
	
	/**
	 * read request line and headers, and invoke function
	 * @param <R> the return type
	 * @param id connection id
	 * @param utf io split tag
	 * @param message buffer
	 * @param c bifunction
	 * @return function result
	 * @throws IOException
	 */
	protected <R> R headRequest(long id,String utf,GByteBuffer message,BiFunction<String, Map<String, String>,R> c,R def) throws IOException {
		DataBuffer db = dbf.getDataBuffer();
		try {
			BufferedReader reader = this.getFrameStreamReader(message,db);
			if(reader==null) {
				return def;
			}
			String requestLine = this.getRequestLine(reader);
			Map<String, String> headers = this.getHeaders(reader);
			logger.info("Connection {} {} {} HEADERS {}",id,utf,requestLine,headers==null?"{}":headers);
			return c.apply(requestLine, headers);
		}finally {
			db.release();
		}
	}
	
	/**
	 * read request line and headers, and invoke the consumer
	 * @param id connection id
	 * @param utf io split tag
	 * @param message buffer
	 * @param c biconsumer
	 * @throws IOException
	 */
	protected void headRequest(long id,String utf,GByteBuffer message,BiConsumer<String, Map<String, String>> c) throws IOException {
		DataBuffer db = dbf.getDataBuffer();
		try {
			BufferedReader reader = this.getFrameStreamReader(message,db);
			if(reader!=null) {
				String requestLine = this.getRequestLine(reader);
				Map<String, String> headers = this.getHeaders(reader);
				logger.info("Connection {} {} {} HEADERS {}",id,utf,requestLine,headers==null?"{}":headers);
				c.accept(requestLine, headers);
			}
		}finally {
			db.release();
		}
	}
	
	/**
	 * build a status notify byte array
	 * @param sessionId connection id
	 * @param code status code
	 * @param message message
	 * @return
	 */
	
	private BufferedReader getFrameStreamReader(GByteBuffer buffer,DataBuffer db) throws UnsupportedEncodingException {
		if(buffer.remaining()<2) {
			return null;
		}
		int len = buffer.readShort();
		if(buffer.remaining()<len) {
			return null;
		}
		byte[] data = db.get();
		if(db.capacity()<len) {
			throw new BufferException("buffer capacity "+db.capacity()+" data length "+len);
		}
		buffer.read(data,0,len);
		ByteArrayInputStream bin = new ByteArrayInputStream(data,0,len);
		return new BufferedReader(new InputStreamReader(bin, context.getCharset()));
	}
	
	private Map<String,String> parseHeaders(Map<String,String> container,String line){
		if(container==null) {
			container = new HashMap<>(8);
		}
		String l = line.trim();
		int index = l.indexOf(":");
		String key = l.substring(0, index).trim();
		String value = l.substring(index+1).trim();
		container.put(key, value);
		return container;
	}
	
	private String getRequestLine(BufferedReader reader) throws IOException {
		return reader.readLine();
	}
	private Map<String, String> getHeaders(BufferedReader reader) throws IOException{
		String line = reader.readLine();
		Map<String, String> headers = null;
		while(StringUtils.isNotBlank(line)&&StringUtils.isNotEmpty(line)) {
			headers = parseHeaders(headers, line);
			line = reader.readLine();
		}
		return headers;
	}
	
	/**
	 * proccess this buffer one time, never do too mush work onBuffer method, just split the work.
	 * @param id connection id
	 * @param s connection
	 * @param b buffer
	 * @return true if bytes are enough to readed, false if buffer is half of packet
	 * @throws IOException
	 */
	protected boolean onBuffer(long id,NioConnection s, GByteBuffer b) throws IOException {return true;};
	
	
}
