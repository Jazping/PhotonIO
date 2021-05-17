package com.vision.niosmart.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.exception.Http3Exception;
import com.vision.niosmart.nio.buffer.GByteBuffer;

public abstract class StreamUtils {
	@Deprecated
	public static byte[] toSegmentBuf(int type,long id,int streamId,int segment,byte[]data) {
		return toSegmentBuf(type,id,streamId,segment,data,0,data.length);
	}
	@Deprecated
	public static byte[] toSegmentBuf(int type,long id,int streamId,int segment,byte[]data,int off,int length) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(out);
		try {
			dout.writeShort(type);
			dout.writeLong(id);
			dout.writeInt(streamId);
			dout.writeInt(segment);
			dout.writeShort(length);
			dout.write(data,off,length);
			dout.close();
			return  out.toByteArray();
		}catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Deprecated
	public static byte[] toBuffer(long id,String utf,byte[]data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(out);
		try {
			dout.writeUTF(utf);
			dout.writeLong(id);
			dout.writeShort(data.length);
			dout.write(data);
			dout.close();
			return out.toByteArray();
		}catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void toBuffer(String utf,byte[]data,DataBuffer db) {
		try {
			db.writeString(utf);
			db.writeShort(data.length);
			db.write(data);
		}catch(ArrayIndexOutOfBoundsException e) {
			throw new BufferException(e);
		}
	}
	
	public static StreamRequestEntity parseRequestLine(long connectionId,String requestLine,int segmentSize) {
		String[] strs = requestLine.split(" ");
		String name = strs[0].trim();
		if(strs.length<4
				||!strs[1].trim().matches("\\d+")
				||!strs[2].trim().matches("\\d+")
				||!strs[3].trim().matches("\\d+")) {
			throw new Http3Exception("illegal request line");
		}
		int stream = Integer.valueOf(strs[1].trim());
		int segments = Integer.valueOf(strs[2].trim());
		long length = Long.valueOf(strs[3].trim());
		boolean udp = false;
		if(strs.length>4) {
			udp = "UDP".equals(strs[4].trim());
		}
		StreamRequestEntity e = new StreamRequestEntity(connectionId,name,stream,segments,length,udp);
		if(strs.length>5) {
			e.setClientId(Long.valueOf(strs[5].trim()));
		}
		return e;
	}
	
	public static byte[] getUTF(GByteBuffer buffer,int minLen, int maxLen) throws IOException {
		if(buffer.remaining()<2) {
			throw new IOException();
		}
		int len = buffer.readShort();
		if(len<minLen||len>maxLen||len>buffer.remaining()) {
			throw new IOException();
		}
		byte[] data = new byte[len];
		buffer.read(data);
		return data;
	}
}
