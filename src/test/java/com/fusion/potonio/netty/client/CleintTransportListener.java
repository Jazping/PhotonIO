package com.fusion.potonio.netty.client;

import com.fusion.potonio.netty.server.MyServerTransportListener;

/**
 * Transport Listener for test of client side
 * @author Jazping
 *
 */
public class CleintTransportListener extends MyServerTransportListener{

	public CleintTransportListener(String saveDir){
		super(saveDir);
	}
}
