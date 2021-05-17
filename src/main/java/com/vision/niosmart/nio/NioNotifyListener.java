package com.vision.niosmart.nio;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.NotifiedSchedulerExecutor;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.ThreadUtil;

public class NioNotifyListener extends NioListener implements NioBufferListener  {
	protected DataTransport<?> tcpt;
	protected UDPDataTransport<?> udpt;
	protected Executor executor;
	protected NotifiedSchedulerExecutor scheduler;
	
	public NioNotifyListener(DataBufferFactory dbf,Context context, DataTransport<?> tcpt, 
			UDPDataTransport<?> udpt,Executor executor,
			NotifiedSchedulerExecutor scheduler,boolean checkId) {
		super(dbf,context,checkId);
		this.tcpt = tcpt;
		this.executor = executor;
		this.udpt = udpt;
		this.scheduler = scheduler;
	}
	
	public NioNotifyListener(DataBufferFactory dbf,Context context, DataTransport<?> tcpt, 
			UDPDataTransport<?> udpt, Executor executor,
			NotifiedSchedulerExecutor udpScheduler) {
		this(dbf,context, tcpt, udpt, executor, udpScheduler, false);
	}
	
	@Override
	protected boolean onBuffer(long id, NioConnection s, GByteBuffer b) throws IOException {
		AtomicBoolean udp = new AtomicBoolean();
		StringBuilder connectionId = new StringBuilder();
		String key = super.headRequest(id, "NOTIFY", b, (line,headers)->{
			String[] strs = line.split(" ");
			if(strs.length>1&&strs[1].trim().equals("200")) {
				udp.set(strs.length>3?"UDP".equals(strs[2]):false);
				if(udp.get()) {
					connectionId.append(strs[4]);
				}
				return udp.get()?strs[3]:strs[2];
			}
			throw new IllegalStateException("illegal request line: "+line);
		},"");
		if(StringUtils.isEmpty(key)) {
			return false;
		}
		SegmentIdempotentStream datagram = context.popDatagramStream(id,udp.get(),key);
		if(datagram!=null) {
			try {
				if(udp.get()) {
					datagram.setConnectionId(Long.valueOf(connectionId.toString()));
					logger.debug("Submit udp write task connection {} stream {} length {}",datagram.getConnectionId(),datagram.getId(),datagram.length());
				}else {
					logger.debug("Submit write task connection {} stream {} length {}",datagram.getConnectionId(),datagram.getId(),datagram.length());
				}
				submitTransport(s,datagram,udp.get());
			} catch (IOException e) {
				logger.warn("drop connection {} stream length {}",id,datagram.length());
				datagram.close();
				datagram.forEach((c)->{
					Stream stream = context.getStream(id, c.getId());
					if(stream!=null) {
						context.removeGetting(stream.getName());
					}
					context.deleteStream(id, c.getId());
				});
			}
		}
		return true;
	}
	
//	private boolean notifyAndGetStatus(long id, NioConnection s, GByteBuffer b) throws IOException {
//		return super.headRequest(id, "NOTIFY", b, (line,headers)->{
//			ThreadUtil.notify(s.getNative());
//			String[] strs = line.split(" ");
//			if(strs.length>1&&strs[1].trim().equals("200")) {
//				return true;
//			}
//			throw new StreamException("stream error: "+line);
//		},false);
//	}
	
	private void submitTransport(NioConnection s,SegmentIdempotentStream stream,boolean udp) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if(!udp) {
						tcpt.transport(s,stream);
						context.removeDatagram(stream.getConnectionId(),stream);
						IOUtils.closeQuietly(stream);
					}else {
						Connection con = context.getConnection(s.getId());
						NioConnection ncon = con.getUdpConnection(false);
						if(con!=null&&ncon!=null&&!stream.allTrue()) {
							int index = udpt.transport(s, stream);
							ncon.flush();
							if(index!=-1&&!stream.allTrue()) {
								scheduler.schedule(()->submitTransport(s, stream, udp), ()->{
									ThreadUtil.wait(stream, udpt.getBatchWait()-150);
									return !stream.allTrue();
								},150,()->!stream.allTrue());
							}
						}
					}
				}catch(OutOfMemoryError e) {
					s.flush();
					logger.warn("MEMORY JAM, {}",e.getMessage());
					logger.debug("Retry connection {} stream {} remain segments {}",stream.getConnectionId(),stream.getId(),stream.remaining());
					scheduler.schedule(()->submitTransport(s, stream, udp), ()->true,100,()->!stream.allTrue());
				} catch (Throwable e) {
					logger.error(e.getMessage(),e);
					logger.error("Transport connection {} stream {} error",stream.getConnectionId(),stream.getId());
					context.removeDatagram(stream.getConnectionId(),stream);
					IOUtils.closeQuietly(stream);
				} finally {
//					pFactory.releaseConnection(s);
				}
			}
		});
	}
}
