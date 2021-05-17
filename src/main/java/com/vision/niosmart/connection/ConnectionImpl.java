package com.vision.niosmart.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import com.vision.niosmart.BigIntegerHashCode;
import com.vision.niosmart.ModedMap;
import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.stream.DefaultSegmentStreamFactory;
import com.vision.niosmart.stream.DefaultStreamFactory;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.IdempotentStreamWrapper;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.SegmentStreamFactory;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.stream.StreamFactory;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.ThreadUtil;

class ConnectionImpl implements Connection {
	private Long id;
	private Long clientId;
	private int remoteReceivePort;
	private int remoteReceiveBuf;
	private int remoteSegSize = 4096;
	private int liveTime = 1000*30;
	private AtomicInteger streamCounter = new AtomicInteger();
	private Map<Integer,Stream> streamMap = new ConcurrentHashMap<>(8);
	private StreamFactory factory = new DefaultStreamFactory();
	private SegmentStreamFactory streamFactory = new DefaultSegmentStreamFactory();
//	private Queue<IdempotentStreamWrapper> dsMap = new ConcurrentLinkedQueue<>();
//	private Queue<IdempotentStreamWrapper> udpQueue = new ConcurrentLinkedQueue<>();
//	private Map<String,IdempotentStreamWrapper> dsMap = new ConcurrentHashMap<>();
//	private Map<String,IdempotentStreamWrapper> udpQueue = new ConcurrentHashMap<>();
	private int sharedMod = 256;
	private ModedMap<BigIntegerHashCode, IdempotentStreamWrapper> dsMap = new ModedMap<>(sharedMod,new StreamModedMapEntryFactory());
	private ModedMap<BigIntegerHashCode, IdempotentStreamWrapper> udpQueue = new ModedMap<>(sharedMod,new StreamModedMapEntryFactory());
	
//	private Context context;

	public ConnectionImpl(long id) {
		super();
		this.id = id;
//		this.context = context;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public boolean containStream(int streamId) {
		return streamMap.containsKey(streamId);
	}
	
	@Override
	public void askStream(int streamId,int segments, String name,int segmentSize,long length) {
		if(id==null) {
			throw new StreamException("stream closed");
		}
		if(streamMap.containsKey(streamId)) {
			throw new StreamException("stream duplicated:"+streamId);
		}
		streamMap.put(streamId, factory.newStream(streamId, segments, name,segmentSize,length));
	}
	
	@Override
	public Stream getStream(int streamId) {
		return streamMap.get(streamId);
	}

	@Override
	public void close() throws IOException {
		this.clear();
	}

	@Override
	public int getRemoteSegSize() {
		return remoteSegSize;
	}

	@Override
	public void setRemoteSegSize(int segmentSize) {
		this.remoteSegSize = segmentSize;
	}

	@Override
	public int getRemoteReceivePort() {
		return remoteReceivePort;
	}

	@Override
	public void setRemoteReceivePort(int remoteReceivePort) {
		this.remoteReceivePort = remoteReceivePort;
	}

	@Override
	public Stream removeStream(int streamId) {
		return streamMap.remove(streamId);
	}

	@Override
	public int getStreamCount() {
		return streamMap.size();
	}

	@Override
	public void setRemoteReceiveBuf(int buf) {
		this.remoteReceiveBuf = buf;
	}

	@Override
	public int getRemoteReceiveBuf() {
		return remoteReceiveBuf;
	}

	@Override
	public int getFixBatchSize() {
		if(this.maxBatch==0) {
			synchronized (fixBatchSize) {
				if(this.maxBatch==0) {
					this.computeFixBatchSize();
				}
			}
		}
		return fixBatchSize.get();
	}
	
	public void incBatchSize() {
		synchronized (fixBatchSize) {
			if(fixBatchSize.get()<maxBatch) {
				fixBatchSize.incrementAndGet();
			}
		}
	}
	
	public void decBatchSize() {
		synchronized (fixBatchSize) {
			if(fixBatchSize.get()>2) {
				fixBatchSize.decrementAndGet();
			}
		}
	}
	
//	private static volatile int interval = UDP_INTERVAL;
//	public int getInterval() {
//		return interval;
//	}
//	public void incInterval() {
//		synchronized (getClass()) {
//			interval++;
//		}
//	}
//	public void decInterval() {
//		synchronized (getClass()) {
//			if(interval>0) {
//				interval--;
//			}
//		}
//	}
	
	private static AtomicInteger fixBatchSize = new AtomicInteger(-1);
	private int maxBatch;
	private void computeFixBatchSize() {
//		int netwide = context.getNetwideLimit();
//		if(netwide>0&&netwide<10) {
//			throw new IllegalStateException("netwide must over 10m");
//		}
//		int max = 200;
//		if(netwide>0) {
//			int normal = netwide;
//			if(getRemoteSegSize()==4096) {
//				max = normal;
//			}else if(getRemoteSegSize()>4096) {
//				max = normal/2;
//			}else {
//				max = normal*(4096/getRemoteSegSize());
//			}
//		}
		this.maxBatch = 200;
		fixBatchSize.set(2);
//		interval = UDP_INTERVAL;
//		if(netwide>0) {
//			interval = 1000/netwide;
//		}
	}
	
	@Override
	public void removeDatagram(int streamId) {
		streamFactory.removeStream(streamId);
	}
	
	@Override
	public int currentDatagramCount() {
		return streamFactory.streamCount();
	}
	
	@Override
	public SegmentIdempotentStream getDatagramStream(int streamId) {
		return streamFactory.getStream(streamId);
	}
	
	@Override
	public SegmentIdempotentStream putDatagramStream(String key,IdempotentStream stream,int segments) throws IOException {
		SegmentIdempotentStream datagram = streamFactory.putStream(id, stream.getId(), stream, segments);
		return putDatagramStream(key,datagram);
	}
	
	@Override
	public SegmentIdempotentStream putDatagramStream(String key,SegmentIdempotentStream datagram) {
//		dsMap.add(new IdempotentStreamWrapper(datagram, System.currentTimeMillis()));
//		dsMap.put(key, new IdempotentStreamWrapper(datagram, System.currentTimeMillis()));
		BigIntegerHashCode hashCode = new BigIntegerHashCode(key.getBytes(),sharedMod);
		IdempotentStreamWrapper w = dsMap.put(hashCode, new IdempotentStreamWrapper(datagram, System.currentTimeMillis()));
		if(w!=null) {
			IOUtils.closeQuietly(w.getStream());
		}
		return datagram;
	}
	
//	private AtomicBoolean popAll = new AtomicBoolean();
	@Override
	public SegmentIdempotentStream popDatagramStream(boolean isUdp,String key) {
		if(key.length()==0) {
			return null;
		}
		BigIntegerHashCode hashCode = new BigIntegerHashCode(key.getBytes(),sharedMod);
		IdempotentStreamWrapper w = isUdp?udpQueue.remove(hashCode):dsMap.remove(hashCode);
		return w==null?null:w.getStream();
		
//		if(!popAll.getAndSet(true)) {
//			try {
//				if(dsMap.size()>1) {
//					CompositeSegmentIdempotentStream s = new CompositeSegmentIdempotentStream(dsMap.values(),remoteSegSize,id);
//					dsMap.clear();
//					return s;
//				}
//				IdempotentStreamWrapper w = dsMap.get(key);
//				return w==null?null:w.getStream();
//			} catch (IOException e) {
//				throw new StreamException(e);
//			} finally {
//				popAll.set(false);
//			}
//		}
//		return null;
	}
	
	public StreamFactory getFactory() {
		return factory;
	}

	public void setFactory(StreamFactory factory) {
		this.factory = factory;
	}

	@Override
	public void clear() {
		this.dsMap.forEachEntry((k,v)->IOUtils.closeQuietly(v.getStream()));
		this.dsMap.clear();
		this.streamFactory.clear();
		this.factory.clear();
		for(Stream s : streamMap.values()) {
			try {
				s.finish();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				s.drop();
			}
		}
		this.streamMap.clear();
//		this.streamCounter.set(0);
	}

	@Override
	public int genStramId() {
		return streamCounter.incrementAndGet();
	}

	@Override
	public SegmentIdempotentStream putDatagramStreamUDP(String key,SegmentIdempotentStream stream) {
//		udpQueue.add(new IdempotentStreamWrapper(stream, System.currentTimeMillis()));
//		udpQueue.put(key, new IdempotentStreamWrapper(stream, System.currentTimeMillis()));
		BigIntegerHashCode hashCode = new BigIntegerHashCode(key.getBytes(),sharedMod);
		IdempotentStreamWrapper w = udpQueue.put(hashCode, new IdempotentStreamWrapper(stream, System.currentTimeMillis()));
		if(w!=null) {
			IOUtils.closeQuietly(w.getStream());
		}
		return stream;
	}

	@Override
	public SegmentIdempotentStream putDatagramStreamUDP(String key,IdempotentStream stream, int segments) throws IOException {
		SegmentIdempotentStream datagram = streamFactory.putStream(id, stream.getId(), stream, segments);
		return putDatagramStreamUDP(key,datagram);
	}

	private UDPDataTransport<? extends Protocol> transport;
	private NioConnection udpConnection;
	private InetAddress address;
	private int port;
	@Override
	public void setUDPTransport(UDPDataTransport<? extends Protocol> transport,InetAddress address,int port) {
		this.transport = transport;
		this.address = address;
		this.port = port;
	}

	Object lock = new Object();
	@Override
	public NioConnection getUdpConnection(boolean init) {
		if(!init) {
			synchronized (lock) {
				while(udpConnection==null) {
					ThreadUtil.wait(lock, 100);
				}
				if(udpConnection.isClosed()) {
					udpConnection = transport.getUdpConnection(address, port);
				}
			}
		}else {
			synchronized (lock) {
				if(udpConnection==null||udpConnection.isClosed()) {
					udpConnection = transport.getUdpConnection(address, port);
				}
				ThreadUtil.notifyAll(lock);
			}
		}
		return udpConnection;
	}
	
	@Override
	public void expireStream() {
		dsMap.forEach((m)->expireStream(m.entrySet().iterator()));
		udpQueue.forEach((m)->expireStream(m.entrySet().iterator()));
	}
	
	private void expireStream(Iterator<Map.Entry<BigIntegerHashCode, IdempotentStreamWrapper>> it) {
		while(it.hasNext()) {
			Map.Entry<BigIntegerHashCode, IdempotentStreamWrapper> e = it.next();
			if(System.currentTimeMillis()>e.getValue().getTimestamp()+liveTime) {
				SegmentIdempotentStream s = e.getValue().getStream();
				LOGGER.warn("Cleaning timeout idempotent stream in queue, id {} connection {}",s.getId(),s.getConnectionId());
				IOUtils.closeQuietly(s);
				it.remove();
			}
		}
	}

	@Override
	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	@Override
	public Long getClientId() {
		return clientId;
	}
}
