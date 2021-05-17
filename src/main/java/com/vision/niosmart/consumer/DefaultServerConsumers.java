package com.vision.niosmart.consumer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.connection.Connections;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioListener;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.IdempotentStreamProvider;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.stream.StreamRequestEntity;
import com.vision.niosmart.stream.StreamUtils;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.NotifiedSchedulerExecutor;
import com.vision.niosmart.transport.TransportMonitor;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.RequestBufferUtils;
import com.vision.niosmart.util.ThreadUtil;

public class DefaultServerConsumers<B> extends NioListener{
	protected DataTransport<?> tcpt;
	protected UDPDataTransport<?> udpt;
	protected IdempotentStreamProvider provider;
	protected Executor executor;
	protected BufferFactory<B> facory;
	protected TransportationListener listener;
	private NotifiedSchedulerExecutor scheduler;
	
	public DefaultServerConsumers(DataBufferFactory dbf,
			Context context,DataTransport<?> tcpt, UDPDataTransport<?> udpt,
			IdempotentStreamProvider provider, BufferFactory<B> facory) {
		super(dbf,context);
		this.tcpt = tcpt;
		this.udpt = udpt;
		this.provider = provider;
		this.facory = facory;
		this.setListener(new TransportationListener() {
			@Override
			public void onError(long connectionId,String status, String resource, String msg) {
				logger.error("{} {} {} {}",connectionId,status,resource,msg);
			}
		});
		
	}
	
	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public NotifiedSchedulerExecutor getScheduler() {
		return scheduler;
	}

	public void setScheduler(NotifiedSchedulerExecutor scheduler) {
		this.scheduler = scheduler;
	}
	
	@Consumer("DATA")
	protected boolean data(NioConnection session, GByteBuffer buffer) throws IOException {
		return tcpt.getIoHandler().doReceive(session,buffer,session.getId());
	}

	@Consumer("NOTIFY")
	protected void notify(@ID long id, NioConnection s, short len, @Param(-2)byte[] data) throws IOException {
		AtomicBoolean udp = new AtomicBoolean();
		StringBuilder connectionId = new StringBuilder();
		AtomicReference<SegmentIdempotentStream> ref = new AtomicReference<>();
		debug(id, len, data,"NOTIFY",(line,headers)->{
			String[] strs = line.split(" ");
			if(strs.length>1&&strs[1].trim().equals("200")) {
				udp.set(strs.length>3?"UDP".equals(strs[2]):false);
				if(udp.get()) {
					connectionId.append(strs[4]);
				}
				String name = udp.get()?strs[3]:strs[2];
				ref.set(context.popDatagramStream(id,udp.get(),name));
			}else if(strs.length>1){
				udp.set(strs.length>3?"UDP".equals(strs[2]):false);
				String name = udp.get()?strs[3]:strs[2];
				tryLog(id,strs[1].trim(),name,"");
				SegmentIdempotentStream datagram = context.popDatagramStream(id,udp.get(),name);
				if(datagram!=null) {
					try {
						datagram.close();
					} catch (IOException e) {
						logger.error(e.getMessage(),e);
						tryLog(id,"500",name,e.getMessage());
					}
				}
			}else {
				tryLog(id,"400","",line);
			}
		});
		SegmentIdempotentStream datagram = ref.get();
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
				throw e;
			}
		}
	}
	
	@Consumer("READ")
	protected GByteBuffer read(@ID long id, NioConnection session, short len, @Param(-2)byte[] data) throws IOException {
		return debug(id, len, data,"READ",(line,headers)->{
			String[] array = line.trim().split(" ");
			String namestr = array[0];
			boolean udp = "UDP".equals(array[1]);
			long connectionId = session.getId();
			Connection connection = context.getConnection(connectionId);
			int segmentSize = connection.getRemoteSegSize();
			B buf = facory.allocate(namestr.length()+50);
			GByteBuffer g = facory.getGByteBuffer(buf, true);
			try {
				IdempotentStream stream = provider.getIdempotentStream(namestr,headers);
				if(stream!=null) {
					stream.setId(connection.genStramId());
					SegmentIdempotentStream datagram = putDatagramStream(udp,connectionId, stream, segmentSize);
					RequestBufferUtils.writeBuffer(datagram.getId(), datagram.getSegments(), namestr, stream.length(),null,udp,session.getId(),g);
				}else {
					RequestBufferUtils.notifyBuffer(String.format("%s 404 %s", array[1],namestr), null, g);
				}
			}catch(IOException e) {
				RequestBufferUtils.notifyBuffer(String.format("%s 500 %s", array[1],namestr), null, g);
			}
			return g;
		});
	}
	
	private SegmentIdempotentStream putDatagramStream(boolean udp, long connectionId, IdempotentStream stream, 
			int segmentSize) throws IOException {
		return udp?context.putDatagramStreamUDP(connectionId, stream.name(),stream, segmentSize):
			context.putDatagramStream(connectionId, stream.name(),stream, segmentSize);
	}
	
	@Consumer("WRITE")
	protected GByteBuffer write(@ID long id, NioConnection session, short len, @Param(-2)byte[] data) throws IOException {
		return debug(id, len, data,"WRITE",(line,headers)->{
			int segmentSize = tcpt.geTransportConf().getMinRead();
			StreamRequestEntity stream = StreamUtils.parseRequestLine(id,line,segmentSize);
			context.askStream(id, stream.getStream(), stream.getSegments(), stream.getName(), segmentSize, stream.getLength());
			String status = stream.isUdp()?"HTTP/3.0 200 UDP "+stream.getName():"HTTP/3.0 200 "+stream.getName();
			if(stream.isUdp()) {
				status = new StringBuilder(status).append(" ").append(session.getId()).toString();
				context.getConnection(id).setClientId(stream.getClientId());
			}
			B buf = facory.allocate(status.length()+20);
			GByteBuffer g = facory.getGByteBuffer(buf, true);
			RequestBufferUtils.notifyBuffer(status, null, g);
			return g;
		});
	}
	
	@Consumer("CONNECT")
	protected GByteBuffer connect(@ID long id, NioConnection session, short len, @Param(-2)byte[] data) throws IOException {
		return debug(id, len, data,"CONNECT",(line,headers)->{
			if(!headers.containsKey("MAX-SEGMENT-SIZE")||
					!headers.get("MAX-SEGMENT-SIZE").matches("\\d+")) {
				session.close();
				throw new ConnectionException("connection rejected for 'MAX-SEGMENT-SIZE'");
			}
			
			if(!headers.containsKey("REV-BUFFER")||
					!headers.get("REV-BUFFER").matches("\\d+")) {
				session.close();
				throw new ConnectionException("connection rejected for 'REV-BUFFER'");
			}
			
			if(headers.containsKey(NioConnection.REMOTE_HOST)) {
				session.setAttribute(NioConnection.REMOTE_HOST, headers.get(NioConnection.REMOTE_HOST));
			}else {
				session.setAttribute(NioConnection.REMOTE_HOST, session.getRemoteAddress().toString());
			}
			
			long nioId = session.getId();
			StringBuilder sb = new StringBuilder("HTTP/3.0 100");
			sb.append("\r\n");
			sb.append("CONNECTION-ID: ");
			sb.append(nioId);
			sb.append("\r\n");
			sb.append("MAX-SEGMENT-SIZE: "+ tcpt.geTransportConf().getMinRead());
			sb.append("\r\n");
			sb.append("REV-BUFFER: " + tcpt.geTransportConf().getReceiveBuffer());
			sb.append("\r\n");
			if(udpt!=null) {
				sb.append("UPORT: " + udpt.getReceivePort());
				sb.append("\r\n");
			}
			sb.append("HEAD-PROTOCOL: id(8b)|UTF('WRITE','READ','NOTIFY','DATA')|data-length(2b)|data");
			sb.append("\r\n");
			sb.append("DATA-PROTOCOL: type(2b)|id(8b)|stream(2b)|segment(4b)|pos(8b)|data-length(2b)|data");
			sb.append("\r\n");
			if(udpt!=null) {
				sb.append("DATA-CONFIRM-PROTOCOL: type(2b)|id(8b)|stream(2b)|segment(4b)|data-length(2b)|1b");
				sb.append("\r\n");
			}
			context.applyForConnectionId(nioId);
			Connection con = context.getConnection(nioId);
			String segStr = headers.get(NioConnection.MAX_SEGMENT_SIZE).trim();
			int segmentSize = Integer.valueOf(segStr);
			if(segmentSize==0||segmentSize%1024!=0) {
				session.close();
				throw new ConnectionException("connection rejected for 'MAX-SEGMENT-SIZE%1024!=0'");
			}
			if(segmentSize>8192) {
				session.close();
				throw new ConnectionException("connection rejected for 'MAX-SEGMENT-SIZE>8192'");
			}
			con.setRemoteSegSize(segmentSize);
			session.setAttribute(NioConnection.MAX_SEGMENT_SIZE, Integer.valueOf(segStr));
			if(headers.containsKey(NioConnection.UPORT)) {
				String tport = headers.get(NioConnection.UPORT).trim();
				con.setRemoteReceivePort(Integer.valueOf(tport));
				session.setAttribute(NioConnection.UPORT, Integer.valueOf(tport));
				InetSocketAddress socketAddress = session.getRemoteAddress();
				con.setUDPTransport(udpt,socketAddress.getAddress(), con.getRemoteReceivePort());
				con.getUdpConnection(true);
			}
			String bufStr = headers.get(NioConnection.REV_BUFFER).trim();
			con.setRemoteReceiveBuf(Integer.valueOf(bufStr));
			session.setAttribute(NioConnection.REV_BUFFER, Integer.valueOf(bufStr));
			Connections.putSession(nioId, session);
			B buf = facory.allocate(sb.length()+20);
			GByteBuffer g = facory.getGByteBuffer(buf, true);
			RequestBufferUtils.buffer("ESTABLISH",sb.toString(), null, g);
			return g;
		});
	}

	protected <R> R debug(long id, short len, byte[] data,String utf,BiFunction<String, Map<String, String>,R> c)
			throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(data,0,len);
		BufferedReader reader = new BufferedReader(new InputStreamReader(bin, context.getCharset()));
		String requestLine = this.getRequestLine(reader);
		Map<String, String> headers = this.getHeaders(reader);
		logger.info("Connection {} {} {} HEADERS {}",id,utf,requestLine,headers==null?"{}":headers);
		return c.apply(requestLine, headers);
	}
	
	protected void debug(long id, short len, byte[] data,String utf,BiConsumer<String, Map<String, String>> c)
			throws IOException {
		debug(id, len, data, utf, (requestLine,headers)->{c.accept(requestLine, headers);return null;});
	}
	
	private String getRequestLine(BufferedReader reader) throws IOException {
		return reader.readLine();
	}
	private Map<String, String> getHeaders(BufferedReader reader) throws IOException{
		String line = reader.readLine();
		Map<String, String> headers = null;
		while(StringUtils.isNotBlank(line)&&StringUtils.isNotEmpty(line)) {
			headers = parseHeaders(headers, line);
			line = reader.readLine();
		}
		return headers;
	}
	
	private Map<String,String> parseHeaders(Map<String,String> container,String line){
		if(container==null) {
			container = new HashMap<>(8);
		}
		String l = line.trim();
		int index = l.indexOf(":");
		String key = l.substring(0, index).trim();
		String value = l.substring(index+1).trim();
		container.put(key, value);
		return container;
	}
	
	private void submitTransport(NioConnection s,SegmentIdempotentStream stream,boolean udp) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Connection con = context.getConnection(s.getId());
					if(con==null) {
						return;
					}
					int interval = Connection.UDP_INTERVAL;
					if(!udp) {
						while(!stream.allTrue()) {
							tcpt.transport(s,stream);
						}
						s.flush();
						logger.info("Transport connection {} stream {} finished",stream.getConnectionId(),stream.getId());
						context.removeDatagram(s.getId(),stream);
						IOUtils.closeQuietly(stream);
						ThreadUtil.notify(con);
					}else {
						NioConnection ncon = con.getUdpConnection(false);
						int batch = -1;
						if(con!=null&&ncon!=null&&!stream.allTrue()) {
							batch = udpt.transport(s, stream);
							ncon.flush();
						}
						if(batch!=-1) {
							int b = batch;
							scheduler.schedule(()->submitTransport(s, stream, udp),()->{
								TransportMonitor monitor = stream.getTransportMonitor();
								if(monitor!=null) {
									int s = monitor.getBatchRemaining();
									if(s==0) {
										con.incBatchSize();
									}else if(s==b){
										con.decBatchSize();
									}
								}
								return !stream.allTrue();
							},interval,(a)->{
								logger.info("Transport connection {} stream {} finished",stream.getConnectionId(),stream.getId());
								context.removeDatagram(s.getId(),stream);
								IOUtils.closeQuietly(stream);
								ThreadUtil.notify(con);
							});
						}else {
							logger.info("Transport connection {} stream {} finished",stream.getConnectionId(),stream.getId());
							context.removeDatagram(s.getId(),stream);
							IOUtils.closeQuietly(stream);
							ThreadUtil.notify(con);
						}
					}
				}catch(OutOfMemoryError e) {
					s.flush();
					logger.warn("MEMORY JAM, {}",e.getMessage());
					logger.debug("Retry connection {} stream {} remain segments {}",stream.getConnectionId(),stream.getId(),stream.remaining());
					scheduler.schedule(()->submitTransport(s, stream, udp), ()->true,100,()->!stream.allTrue());
				} catch (Throwable e) {
					e.printStackTrace();
					logger.error(e.getMessage(),e);
					logger.error("Transport connection {} stream {} error",stream.getConnectionId(),stream.getId());
					context.removeDatagram(stream.getConnectionId(),stream);
					IOUtils.closeQuietly(stream);
					tryLog(s.getId(),"500",stream.name(),e.getMessage());
				} 
			}
		});
	}
	
	protected void tryLog(long connectionId,String status,String res,String msg) {
		if(context.removeGetting(res)) {
			logger.error("Remove waiting {} {} {} {}",connectionId,status,res,msg);
		}
		if(listener!=null) {
			listener.onError(connectionId,status, res, msg);
		}
	}

	public TransportationListener getListener() {
		return listener;
	}

	public void setListener(TransportationListener listener) {
		this.listener = listener;
	}
}
