package com.vision.niosmart.util;

import io.netty.buffer.ByteBuf;

public class PioUtil {
	
	public static ByteBuf msg(String head, String msg, ByteBuf direct) {
		return msg(head.getBytes(),msg==null?null:msg.getBytes(),direct);
	}
	
	public static ByteBuf msg(byte[] head, byte[] protocol, ByteBuf direct) {
		form(head,direct);
		form(protocol,direct);
		return direct;
	}
	
	public static ByteBuf msgPro(byte[] head, ByteBuf protocol, ByteBuf direct) {
		form(head,direct);
		direct.writeBytes(protocol);
		return direct;
	}
	
	public static ByteBuf msgPro(String head, ByteBuf protocol, ByteBuf direct) {
		return msgPro(head.getBytes(),protocol,direct);
	}
	
	public static ByteBuf form(byte[] data,ByteBuf buffer) {
		if(data==null) {
			buffer.writeShort(-1);
		}else {
			buffer.writeShort(data.length);
			buffer.writeBytes(data);
		}
		return buffer;
	}
}
