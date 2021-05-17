package com.vision.niosmart.client;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.client.GenericNioExceptionHandler.HandleValue;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioConnector;
import com.vision.niosmart.nio.NioExceptionHandler;
import com.vision.niosmart.nio.NioFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.util.ThreadUtil;

/**
 * base nio smart client, provide datagram sending
 * @author Jazping
 *
 * @param <B> which nio type to use
 */
public class BaseNioSmartClient<B> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private GenericNioExceptionHandler handler = new GenericNioExceptionHandler();
	protected NioFactory factor;
	protected NioConfiguration configuration;
	protected NioConnector connector;
	protected BufferFactory<B> bfactory;
	protected DataBufferFactory dbf;
	protected int timeoutCount;
	protected ClientPool<B> pool;

	public BaseNioSmartClient(ClientPool<B> pool) {
		super();
		this.pool = pool;
	}
	
	public BaseNioSmartClient(NioFactory factor, NioConfiguration configuration,
			BufferFactory<B> bfactory,DataBufferFactory dbf,ClientPool<B> pool) {
		super();
		this.factor = factor;
		this.configuration = configuration;
		this.bfactory = bfactory;
		this.dbf = dbf;
		this.pool = pool;
		connect();
	}

	protected synchronized NioConnection connect() {
		if(connector==null) {
			connector = factor.getConnector(configuration);
		}
		NioConnection connection =  connector.connect();
		if(connection==null) {
			throw new ConnectionException("Can't fetch connection");
		}
		connection.setExceptionHanlder(handler);
		return connection;
	}

	/**
	 * send packet to server and flush, tcp only
	 * @param b the direct buffer
	 * @throws IOException
	 */
	public final void sendFlush(B b) throws IOException {
		this.send(b);
		this.flush();
	}
	
	/**
	 * send packet to server and flush, tcp only
	 * @param b the direct buffer
	 * @throws IOException
	 */
	public final void synSendFlush(B b) throws IOException {
		this.synsend(b);
		this.flush();
	}
	
	/**
	 * send packet to server, tcp only
	 * @param b the direct buffer
	 * @throws IOException
	 */
	public final void send(B b) throws IOException {
		NioConnection session = connect();
		session.write(b);
	}
	/**
	 * send packet in synchorous
	 * @param b the direct buffer
	 * @throws IOException
	 */
	public final void synsend(B b) throws IOException {
		NioConnection session = connect();
		session.synWrite(b);
	}
	
	/**
	 * return buffer for sending if buffer pool is direct, then return direct buffer
	 * @param length
	 * @return
	 */
	public final B allcSendBuf(int length) {
		return bfactory.allocate(length);
	}
	
	/**
	 * flush sending, writing and reading
	 */
	public final void flush() {
		NioConnection session = connector.connect();
		if(!session.isClosed()) {
			session.flush();
		}
	}

	protected long getConnectionId() {
		return connector.currentId();
	}

	public final synchronized void shutdown() {
		if(!connector.isDisposed()) {
			configuration.getContext().closeConnection(getConnectionId());
			if(null!=configuration.getUdpTransport()) {
				configuration.getUdpTransport().close();
			}
			connector.disponse();
			if(pool!=null) {
				pool.dropObject(this);
			}
		}
	}

	/**
	 * get the nio factory
	 * @return
	 */
	public NioFactory getFactor() {
		return factor;
	}

	/**
	 * set the nio factory, only effected before connector build
	 * @param factor
	 */
	public void setFactor(NioFactory factor) {
		this.factor = factor;
	}

	/**
	 * get the nio configuration
	 * @return
	 */
	public NioConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * set the nio configuration, only effected before connector build
	 * @param configuration
	 */
	public void setConfiguration(NioConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * blocking and await all operation completed
	 * @param timeout max time to wait, request 1000+ ms
	 */
	public final synchronized void awaitComplete(int timeout) {
		if(timeout<1000) {
			throw new IllegalArgumentException();
		}
		this.flush();
		Long connectionId = getConnectionId();
		Context context = configuration.getContext();
		long currentTime = System.currentTimeMillis();
		while(context.currentStreamCount(connectionId)!=0||
				context.currentDatagramCount(connectionId)!=0||
				configuration.getContext().hasGetting()) {
			ThreadUtil.wait(context.getConnection(connectionId), 1000);
			if(System.currentTimeMillis()>currentTime+timeout) {
				int c1  = context.currentStreamCount(connectionId);
				int c2 = context.currentDatagramCount(connectionId);
				boolean hasGet = configuration.getContext().hasGetting();
				timeoutCount++;
				clean();
				throw new ConnectionException("timeout catched connection "+getConnectionId()+", streamCount "+c1+" datagramCount "+c2+" hasGet "+hasGet);
			}
		}
	}
	
	protected synchronized void clean() {
		Long connectionId = getConnectionId();
		Context context = configuration.getContext();
//		int c1  = context.currentStreamCount(connectionId);
//		int c2 = context.currentDatagramCount(connectionId);
//		boolean hasGet = configuration.getContext().hasGetting();
//		logger.debug("clean connection {}, streamCount {} datagramCount {} hasGet {}",getConnectionId(),c1,c2,hasGet);
		configuration.getContext().clearGetting();
		context.getConnection(connectionId).clear();
	}
	
	public void release() {
		if(pool==null) {
			throw new NullPointerException("pool required");
		}
		pool.release(this);
	}
	
	public <E extends Throwable> void setExceptionHandler(String name, Class<E> type, NioExceptionHandler handler) {
		this.handler.setHandle(name, type, handler);
	}
	
	public void removeExceptionHandle(String name) {
		this.handler.removeHandle(name);
	}
	
	public void setAll(Map<String,HandleValue> map) {
		this.handler.setAll(map);
	}
}