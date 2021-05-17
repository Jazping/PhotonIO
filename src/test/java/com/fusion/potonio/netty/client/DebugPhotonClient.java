package com.fusion.potonio.netty.client;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

import com.vision.niosmart.transport.TransportListener;


/**
 * non-SSL PhotonIO client, run test without hard disk writing
 * @author Jazping
 *
 */
public class DebugPhotonClient extends PhotonClient{

	public DebugPhotonClient(InetSocketAddress remoteAddress, InetSocketAddress localAddress, String saveDir) {
		super(remoteAddress, localAddress, saveDir);
	}
	
	@Override
	protected Supplier<TransportListener> transportListener(){
		return () -> new DebugClientTransportListener(saveDir + counter.getAndIncrement());
	}
	
	public static void main(String[] args) throws Exception {
		InetSocketAddress remoteAddress = new InetSocketAddress("192.168.1.100", 8001);
		InetSocketAddress localAddress = new InetSocketAddress("192.168.1.100", 0);
		String saveDir = "C:\\Users\\Admin\\Desktop\\cli\\c";
		new DebugPhotonClient(remoteAddress,localAddress,saveDir).start();
		
	}

	protected int lapforEachClient(int clientCount) {
		return clientCount*1000;
	}
}
