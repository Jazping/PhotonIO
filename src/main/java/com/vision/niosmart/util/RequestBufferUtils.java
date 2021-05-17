package com.vision.niosmart.util;

import java.util.function.Supplier;

import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBuffer;

public class RequestBufferUtils {
	
	public static void writeBuffer(int id,int segments,String name,long length,StringBuilder headers,boolean udp, long cid, DataBuffer db) {
		StringBuilder sb = new StringBuilder().append(name).append(" ").append(id).append(" ").append(segments);
		sb.append(" ").append(length).append(udp?" UDP "+cid+" ":" ");
		requestBuffer("WRITE",headers,()->sb,db);
	}
	
	public static void readBuffer(String name,StringBuilder headers,boolean udp, long cid, DataBuffer db) {
		requestBuffer("READ",headers,()->new StringBuilder().append(name).append(udp?" UDP "+cid+" ":" ").append("HTTP/3.0"),db);
	}
	
	public static void notifyBuffer(String status,StringBuilder headers,DataBuffer db) {
		requestBuffer("NOTIFY",headers,()->new StringBuilder().append(status),db);
	}
	
	public static void buffer(String tag,String status,StringBuilder headers,DataBuffer db) {
		requestBuffer(tag,headers,()->new StringBuilder().append(status),db);
	}
	
	private static void requestBuffer(String method,StringBuilder headers,Supplier<StringBuilder> s,DataBuffer db) {
		try {
			db.writeString(method);
			StringBuilder sb = s.get();
			sb.append("\r\n");
			if(headers!=null) {
				sb.append(headers);
			}
			db.writeString(sb.toString());
		}catch(ArrayIndexOutOfBoundsException e) {
			throw new BufferException(e);
		}
	}
	
	
	public static void writeBuffer(int id,int segments,String name,long length,StringBuilder headers,boolean udp, long cid, GByteBuffer db) {
		StringBuilder sb = new StringBuilder().append(name).append(" ").append(id).append(" ").append(segments);
		sb.append(" ").append(length).append(udp?" UDP "+cid+" ":" ");
		requestBuffer("WRITE",headers,()->sb,db);
	}
	
	public static void readBuffer(String name,StringBuilder headers,boolean udp, long cid, GByteBuffer db) {
		requestBuffer("READ",headers,()->new StringBuilder().append(name).append(udp?" UDP "+cid+" ":" ").append("HTTP/3.0"),db);
	}
	
	public static void notifyBuffer(String status,StringBuilder headers,GByteBuffer db) {
		requestBuffer("NOTIFY",headers,()->new StringBuilder().append(status),db);
	}
	
	public static void buffer(String tag,String status,StringBuilder headers,GByteBuffer db) {
		requestBuffer(tag,headers,()->new StringBuilder().append(status),db);
	}
	
	private static void requestBuffer(String method,StringBuilder headers,Supplier<StringBuilder> s,GByteBuffer db) {
		try {
			db.writeString(method);
			StringBuilder sb = s.get();
			sb.append("\r\n");
			if(headers!=null) {
				sb.append(headers);
			}
			db.writeString(sb.toString());
		}catch(ArrayIndexOutOfBoundsException e) {
			throw new BufferException(e);
		}
	}
}
