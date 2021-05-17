package com.vision.niosmart;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.connection.ConnectionFactory;
import com.vision.niosmart.connection.DefaultConnectionFactory;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.stream.CompositeSegmentIdempotentStream;
import com.vision.niosmart.stream.DefaultSegmentStreamFactory;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.SegmentIdempotentStream;
import com.vision.niosmart.stream.Stream;


public class DefaultContext implements Context {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Map<Long, Connection> connectionMap = new ConcurrentHashMap<>();
	private Map<Long, ConcurrentLinkedQueue<SegmentIdempotentStream>> dsMap = new ConcurrentHashMap<>();
	private Map<Long, DefaultSegmentStreamFactory> dsMap2 = new ConcurrentHashMap<>();
	private Map<String,Boolean> gettingMap = new ConcurrentHashMap<>();
	private Map<InetSocketAddress, Boolean> iptables = new Hashtable<>();
	private ConnectionFactory factory = new DefaultConnectionFactory();
	public DefaultContext() {
		Timer timer = new Timer("NioStreamCleaner",true);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				connectionMap.forEach((k,v)->v.expireStream());
			}
		};
		timer.schedule(task, 10000, 10000);
	}
	@Override
	public long applyForConnectionId(long id) throws ConnectionException {
		if(connectionMap.get(id)==null) {
			synchronized (connectionMap) {
				if(connectionMap.get(id)==null) {
					connectionMap.put(id, factory.newConnection(id));
				}
			}
		}
		return id;
	}

	@Override
	public boolean isConnectionAvailable(long connectionId) {
		return connectionMap.containsKey(connectionId);
	}

	@Override
	public void askStream(long connectionId, int streamId,int segments, String name,int segmentSize,long length) throws StreamException {
		if(!isConnectionAvailable(connectionId)) {
			throw new StreamException("connection not found: "+connectionId);
		}
		Connection connection = connectionMap.get(connectionId);
		connection.askStream(streamId,segments,name,segmentSize,length);
	}

	@Override
	public boolean isStreamAvailable(long connectionId,int streamId) {
		if(!isConnectionAvailable(connectionId)) {
			return false;
		}
		return connectionMap.get(connectionId).containStream(streamId);
	}

	@Override
	public boolean isStreamFully(long connectionId, int streamId) {
		if(!isConnectionAvailable(connectionId)) {
			throw new ConnectionException("connection not found");
		}
		if(!connectionMap.get(connectionId).containStream(streamId)) {
//			throw new StreamException("stream not found");
			return true;
		}
		return connectionMap.get(connectionId).getStream(streamId).isFully();
	}

	@Override
	public void closeConnection(long connectionId) {
		if(isConnectionAvailable(connectionId)) {
			try {
				ConcurrentLinkedQueue<SegmentIdempotentStream> list = dsMap.remove(connectionId);
				if(list!=null) {
					for(SegmentIdempotentStream ds : list) {
						logger.info("drop connection {} segment idempotent stream[input] {} allTrue {}",connectionId,ds.getId(),ds.allTrue());
						ds.close();
					}
				}
				DefaultSegmentStreamFactory map = dsMap2.remove(connectionId);
				if(map!=null) {
					for(SegmentIdempotentStream ds : map.allStreams()) {
						logger.info("drop connection {} segment idempotent stream[output] {} has remaining {}",connectionId,ds.getId(),ds.remaining());
						ds.close();
					}
				}
				connectionMap.get(connectionId).close();
			} catch (IOException e) {
				throw new ConnectionException("closing connection error",e);
			} finally {
				connectionMap.remove(connectionId);
			}
		}
	}

	@Override
	public boolean fillStream(long connectionId, int streamId, int segments, long pos,byte[] data,int offset,int length) throws StreamException {
		if(!isConnectionAvailable(connectionId)) {
			throw new ConnectionException("connection not found");
		}
		if(!connectionMap.get(connectionId).containStream(streamId)) {
			return false;
		}
		try {
			return connectionMap.get(connectionId).getStream(streamId).put(segments, pos,data,offset,length);
		} catch (IOException e) {
			throw new StreamException(e.getMessage(),e);
		}
	}

	@Override
	public boolean finishStream(long connectionId, int streamId) throws IOException {
		checkStream(connectionId,streamId);
		return connectionMap.get(connectionId).getStream(streamId).finish();
	}

	@Override
	public Stream getStream(long connectionId, int streamId) {
		return connectionMap.get(connectionId).getStream(streamId);
	}
	
	private void checkStream(long connectionId, int streamId) {
		if(!isConnectionAvailable(connectionId)) {
			throw new ConnectionException("connection not found");
		}
		if(!connectionMap.get(connectionId).containStream(streamId)) {
			throw new StreamException("stream not found");
		}
	}

	@Override
	public String getCharset() {
		return "utf-8";
	}

	@Override
	public Connection getConnection(long connectionId) {
		return connectionMap.get(connectionId);
	}

	@Override
	public SegmentIdempotentStream putDatagramStream(long id, String key,IdempotentStream stream,int segments) throws IOException {
		Connection con = connectionMap.get(id);
		if(con!=null) {
			return con.putDatagramStream(key, stream, segments);
		}
		return null;
	}
	
	@Override
	public SegmentIdempotentStream putDatagramStreamUDP(long id, String key,IdempotentStream stream, int segmentSize)
			throws IOException {
		Connection con = connectionMap.get(id);
		if(con!=null) {
			return con.putDatagramStreamUDP(key,stream, segmentSize);
		}
		return null;
	}
	
	@Override
	public SegmentIdempotentStream popDatagramStream(long id,boolean isUdp,String key) {
		Connection con = connectionMap.get(id);
		if(con!=null) {
			return con.popDatagramStream(isUdp,key);
		}
		return null;
	}
	
	@Override
	public SegmentIdempotentStream getDatagramStream(long id, int stream) {
		Connection con = connectionMap.get(id);
		if(con!=null) {
			return con.getDatagramStream(stream);
		}
		return null;
	}
	
	@Override
	public Stream deleteStream(long connectionId, int streamId) {
		Connection con = getConnection(connectionId);
		if(con!=null) {
			return con.removeStream(streamId);
		}
		return null;
	}

	@Override
	public void removeDatagram(long id, SegmentIdempotentStream stream) {
		Connection con = connectionMap.get(id);
		if(con!=null) {
			if(CompositeSegmentIdempotentStream.class.isInstance(stream)) {
				CompositeSegmentIdempotentStream composite = (CompositeSegmentIdempotentStream) stream;
				composite.forEach((c)->con.removeDatagram(c.getId()));
			}else {
				con.removeDatagram(stream.getId());
			}
		}
	}

	@Override
	public int currentStreamCount(long connectionId) {
		Connection con = connectionMap.get(connectionId);
		if(con==null) {
			return 0;
		}
		return con.getStreamCount();
	}

	@Override
	public int currentDatagramCount(long connectionId) {
		Connection con = connectionMap.get(connectionId);
		if(con==null) {
			return 0;
		}
		return con.currentDatagramCount();
	}

	@Override
	public void putGetting(String name) {
		gettingMap.put(name, true);
	}

	@Override
	public boolean removeGetting(String name) {
		if(name!=null) {
			Boolean b = gettingMap.remove(name);
			return b==null?false:b;
		}
		return false;
	}

	@Override
	public boolean hasGetting() {
		return !gettingMap.isEmpty();
	}
	@Override
	public void putAddress(InetSocketAddress address) {
		iptables.put(address, true);
	}
	@Override
	public boolean isAvailable(InetSocketAddress address) {
		return iptables.containsKey(address);
	}
	@Override
	public void removeAddress(InetSocketAddress address) {
		if(address!=null) {
			iptables.remove(address);
		}
	}
	@Override
	public int connectionCount() {
		return connectionMap.size();
	}
	@Override
	public void clearGetting() {
		gettingMap.clear();
	}
}
