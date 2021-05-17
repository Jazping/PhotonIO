package com.fusion.potonio.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.consumer.Consumer;
import com.vision.niosmart.consumer.ID;
import com.vision.niosmart.consumer.Param;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.nio.buffer.NettyBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.util.PioUtil;

import io.netty.buffer.ByteBuf;

/**
 * custom consumer for server
 * @author Jazping
 *
 */
public class MyServerConsumers {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected DataBufferFactory dbf;
	protected BufferFactory<ByteBuf> factory;
	
	public MyServerConsumers(BufferFactory<ByteBuf> factory) {
		this.factory = factory;
	}
	
	@Consumer(value="SAYHELLO")
	protected GByteBuffer helloworld(short len, @ID long id, @Param(0)String data) {
		ByteBuf buf = PioUtil.msg("HELLOANWER", "hello, ".concat(data), factory.allocate(100));
		logger.info("Connection {} MESSAGE [SAYHELLO]: {}",id,data);
		return new NettyBuffer(buf);
	}
	
	@Consumer(value="SAYHELLO2")
	protected GByteBuffer helloworld2(@Param(value=2,truncate="-1")short len, @ID long id, @Param(0)String data) {
		if(data==null||data=="") {
			data = "annious";
		}
		ByteBuf buf = PioUtil.msg("HELLOANWER", "hello, ".concat(data), factory.allocate(100));
		logger.info("Connection {} MESSAGE [SAYHELLO2]: {}",id,data);
		return new NettyBuffer(buf);
	}
}
