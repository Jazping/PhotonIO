package com.fusion.potonio.netty.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.nio.NioException;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.transport.TransportListener;

/**
 * Transport Listener for test of server side
 * @author Jazping
 *
 */
public class MyServerTransportListener implements TransportListener{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected File dir = null;
	protected static final ThreadLocal<byte[]> BYTELOCAL = new ThreadLocal<>();
	public MyServerTransportListener(String saveDir) {
		dir = new File(saveDir);
		if(!dir.exists()&&!dir.mkdirs()) {
			throw new NioException("directory "+saveDir+" unwritable");
		}
	}
	
	@Override
	public void onFinished(Stream stream) {
		OutputStream out = null;
		try {
			String name = stream.getName().replaceAll("[\\|/]", "%20%");
			out = new FileOutputStream(new File(dir,name));
			stream.saveTo(out,getLocalBytes());
		}catch(IOException e) {
			logger.error("save file error: "+stream.getName(),e);
		}finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	protected byte[] getLocalBytes() {
		byte[] buffer = BYTELOCAL.get();
		if(buffer==null) {
			buffer = new byte[1024*64];
			BYTELOCAL.remove();
			BYTELOCAL.set(buffer);
		}
		return buffer;
	}

	@Override
	public void onRepeat(InetSocketAddress destination,long connectionId, int streamId, int segment) {
		logger.debug("duplicate stream {} connecton {} segment {}",streamId,connectionId,segment);
	}

	@Override
	public void connectionNotFound(InetSocketAddress destination, long connectionId) {
		logger.error("connection not found: {}",connectionId);
	}
}
