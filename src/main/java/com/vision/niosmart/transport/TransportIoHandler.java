package com.vision.niosmart.transport;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.exception.ProtocolException;
import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DataBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.Stream;

public abstract class TransportIoHandler<E extends Protocol> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected Context context;
	private TransportListener listener;
	private DataBufferFactory dataBufferFactory;
	private DataProtocol<E> dp;
	
	public TransportIoHandler(Context context, DataBufferFactory df,DataProtocol<E> dp) {
		super();
		this.context = context;
		this.dataBufferFactory = df;
		this.dp = dp;
	}

	protected boolean filterReceived(NioConnection session) {
		return true;
	}
	
	public TransportListener getListener() {
		return listener;
	}

	public void setListener(TransportListener listener) {
		this.listener = listener;
	}
	public boolean doReceive(NioConnection session,GByteBuffer buf,long connectionId)  {
		try {
			InetSocketAddress fromAddress = (InetSocketAddress) session.getRemoteAddress();
			E protocol = dp.dumpProtocol(connectionId,fromAddress, buf);
			dp.beforeRecvice(this,session, context,protocol);
			if(dp.isAvailable(context, protocol)) {
				DataBuffer dbf = null;
				try {
					int length = protocol.dataLength();
					if(buf.remaining()<length) {
						return false;
					}
					dbf = dataBufferFactory.getDataBuffer();
					if(dbf.capacity()<length) {
						throw new IOException("buffer capacity small than data length "+dbf.capacity()+" < "+length) ;
					}
					byte[] packet = dbf.get();
					buf.read(packet,0,length);
					if (!dp.isFully(context, protocol)&& dp.recvice(context, protocol,packet,0,length)) {
						if(dp.isFully(context, protocol) && dp.finish(context, protocol)) {
							Stream stream = dp.cleanUp(context, protocol);
							if(stream!=null) {
								dp.transfer(stream);
							}
						}
					}else {
						dp.onDataRepeat(fromAddress,context, protocol);
					}
				}catch (IOException e) {
					throw e;
				}catch(Exception e) {
					throw new IOException(e);
				}finally {
					dbf.release();
				}
			}else {
				onConnectionLose((InetSocketAddress) session.getRemoteAddress(), connectionId);
			}
		}catch(ProtocolException e) {
			return false;
		}catch (IOException e) {
			throw new StreamException(e);
		}
		return true;
	}
	
	private void onConnectionLose(InetSocketAddress destination, long connectionId) {
		if(this.listener!=null) {
			this.listener.connectionNotFound(destination,connectionId);
		}
	}

	protected abstract void confirm(GByteBuffer buf) throws IOException ;
	public abstract void onReceive(long connectionId, 
			int streamId, int segment) throws IOException;
}
