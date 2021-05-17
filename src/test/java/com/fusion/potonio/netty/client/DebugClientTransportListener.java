package com.fusion.potonio.netty.client;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.fusion.potonio.netty.server.MyServerTransportListener;
import com.vision.niosmart.stream.Stream;

/**
 * client transporter listener without hard disk writing.
 * @author Jazping
 *
 */
public class DebugClientTransportListener extends MyServerTransportListener{

	public DebugClientTransportListener(String saveDir) {
		super(saveDir);
	}

	@Override
	public void onFinished(Stream stream) {
		OutputStream out = null;
		try {
			out = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					
				}
			};
			stream.saveTo(out,getLocalBytes());
		}catch(IOException e) {
			logger.error("save file error: "+stream.getName(),e);
		}finally {
			IOUtils.closeQuietly(out);
		}
	}
}
