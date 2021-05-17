package com.fusion.potonio.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.consumer.Consumer;
import com.vision.niosmart.consumer.ID;
import com.vision.niosmart.consumer.Param;
import com.vision.niosmart.stream.DataBufferFactory;

/**
 * custom consumer for client
 * @author Jazping
 *
 */
public class MyClientConsumers {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected DataBufferFactory dbf;
	
	public MyClientConsumers(DataBufferFactory dbf) {
		this.dbf = dbf;
	}
	
	@Consumer(value="HELLOANWER")
	protected void helloworld(@ID long id, short len, @Param(-1)String data) {
		System.err.println("message: "+data);
	}
}
