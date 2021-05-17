package com.fusion.potonio.netty.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fusion.potonio.netty.TestUtil;
import com.vision.niosmart.client.ClientPool;
import com.vision.niosmart.client.NioSmartClient;
import com.vision.niosmart.client.Parameters;
import com.vision.niosmart.client.PooledClientFactory;
import com.vision.niosmart.consumer.BaseCodecFactory;
import com.vision.niosmart.consumer.DefaultClientConsumers;
import com.vision.niosmart.consumer.TransportationListener;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.NioAnnotationListener;
import com.vision.niosmart.nio.NioBufferListener;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.NioException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.NettyBufferFactory;
import com.vision.niosmart.nio.netty.NettyConnectionFactory;
import com.vision.niosmart.nio.netty.NettyFactory;
import com.vision.niosmart.nio.netty.NettyUDPTransport;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.DefaulDataProtocol;
import com.vision.niosmart.protocol.DefaultListenerProtocol;
import com.vision.niosmart.protocol.DefaultStreamProtocol;
import com.vision.niosmart.protocol.Meta;
import com.vision.niosmart.protocol.Protocol;
import com.vision.niosmart.protocol.StreamProtocol;
import com.vision.niosmart.protocol.UdpDataProtocol;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.DefaultDataBufferFactory;
import com.vision.niosmart.stream.FileIdempotentStream;
import com.vision.niosmart.stream.IdempotentStreamProvider;
import com.vision.niosmart.transport.AbstractDataTransport;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.DefaultTransportConf;
import com.vision.niosmart.transport.NotifiedSchedulerExecutor;
import com.vision.niosmart.transport.TCPDataTransport;
import com.vision.niosmart.transport.TransportConf;
import com.vision.niosmart.transport.TransportListener;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.PioUtil;
import com.vision.niosmart.util.ReflectionUtil;
import com.vision.niosmart.util.SchedulerUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * non-SSL PhotonIO client
 * @author Jazping
 *
 */
public class PhotonClient extends Thread{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected AtomicInteger counter = new AtomicInteger();
	protected AtomicInteger portCounter = new AtomicInteger();
	protected InetSocketAddress remoteAddress = null;
	protected InetSocketAddress localAddress = null;
	protected AtomicInteger runningCounter = new AtomicInteger();
	protected String saveDir = null;
	
	public PhotonClient(InetSocketAddress remoteAddress,InetSocketAddress localAddress,String saveDir) {
		this.remoteAddress = remoteAddress;
		this.localAddress = localAddress;
		this.saveDir = saveDir;
		this.setName("PhotonIO-Demo-Client");
	}

	public static void main(String[] args) throws Exception {
		InetSocketAddress remoteAddress = new InetSocketAddress("192.168.1.100", 8001);
		InetSocketAddress localAddress = new InetSocketAddress("192.168.1.100", 0);
		String saveDir = "C:\\Users\\Admin\\Desktop\\cli\\c";
		new PhotonClient(remoteAddress,localAddress,saveDir).start();
		
	}
	
	public void run() {
		DataBufferFactory pdf = buildDataBufferFactory();
		Parameters<ByteBuf> ps = build(pdf);
		int numThread = Runtime.getRuntime().availableProcessors()*1;
		ClientPool<ByteBuf> test = clientPool(numThread, ps);
		//just do one lap of work on each client 
		//if more than one, it just repeat the same working
		int excuteCount = lapforEachClient(numThread);
		long t = System.currentTimeMillis();
		ThreadPoolExecutor executorForTest = createTestExecutor(numThread);
		LinkedList<Future<?>> list = new LinkedList<>();
		for (int i = 0; i < excuteCount; i++) {
			int c = i;
			if (list.size() > 1024*2) {
				list.poll();//prevent memory overflow
			}
			//submit one lap of work for each client
			list.add(executorForTest.submit(newRunnable(pdf,test,c)));
		}
		for (Future<?> f : list) {
			try {
				f.get();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			} catch (ExecutionException e) {
				logger.error(e.getMessage(), e);
			}
		}
		executorForTest.shutdown();
		test.close();
		System.err.println("readed file count: "+excuteCount*21);
		System.err.println("cost time(s): "+(System.currentTimeMillis()-t)/1000f);
		System.err.println("shutdown: " + runningCounter.get() + " " + (runningCounter.get() == excuteCount));
	}
	
	protected int lapforEachClient(int clientCount) {
		return clientCount*1;
	}
	
	/**
	 * create client pool for testing work. client count, the more client number, 
	 * the more virtual machine memory required.
	 * the more virtual machine initiated memory configuration, the less time required, 
	 * because of this way can pare the memory allocation time.
	 * @param numThread
	 * @param ps
	 * @return
	 */
	protected ClientPool<ByteBuf> clientPool(int numThread, Parameters<ByteBuf> ps){
		PooledClientFactory<Channel,ByteBuf> pcf = new PooledClientFactory<>(ps);
		return new ClientPool<>(pcf, numThread);
	}
	
	/**
	 * build client configuration parameters
	 * @param pdf
	 * @return
	 */
	protected Parameters<ByteBuf> build(DataBufferFactory pdf) {
		PooledByteBufAllocator ioAllocator = new PooledByteBufAllocator(true);
		BufferFactory<ByteBuf> factory = buildBufferFactory(ioAllocator);
		TransportConf conf = buildTransportConf();
		NioConnectionFactory<Channel> cFactory = new NettyConnectionFactory(conf);
		NettyFactory nioFactory = new NettyFactory(factory, ioAllocator, cFactory);
		Supplier<TransportListener> supplier = transportListener();
		Supplier<Executor> workSupplier = workSupplier();
		Consumer<Executor> shutDown = shutdownConsumer();
		NotifiedSchedulerExecutor scheduler = notifiedScheduler();
		Function<NioConfiguration, StreamProtocol> fn = streamProtocol(scheduler, pdf, factory);
		Function<NioConfiguration, DataTransport<? extends Protocol>> tcpFn = dataTransport(conf, pdf, factory);
		Function<NioConfiguration, UDPDataTransport<? extends Protocol>> udpFn = udpDataTransport(conf, factory, pdf, cFactory, ioAllocator);
		Parameters<ByteBuf> ps = new Parameters<>();
		ps.setBindAddress(localAddress);
		ps.setBufferFactory(factory);
		ps.setDataBufferFactory(pdf);
		ps.setDataTransportFn(tcpFn);
		ps.setDelay(0);
		ps.setListenerSpl(supplier);
		ps.setLocalAddress(localAddress);
		ps.setNioFactory(nioFactory);
		ps.setRemoteAddress(remoteAddress);
		ps.setShutDownCsm(shutDown);
		ps.setWorkerSpl(workSupplier);
		ps.setUdpDataTransportFn(udpFn);
		ps.setStreamProtocolFn(fn);
		ps.setAlloc(ioAllocator);
		try {
			afterParameterSet(ps);
			return ps;
		} catch (IOException e) {
			throw new NioException(e);
		}
	}
	
	/**
	 * will be invoked after client side configuration setting
	 * @param ps
	 * @throws IOException
	 */
	protected void afterParameterSet(Parameters<ByteBuf> ps) throws IOException{
		
	}
	
	/**
	 * transportation buffer allocator factory
	 * @param ioAllocator
	 * @return
	 */
	protected BufferFactory<ByteBuf> buildBufferFactory(ByteBufAllocator ioAllocator){
		int bufferSize = 4096 + AbstractDataTransport.PROTOCOL_LENGTH;
		return new NettyBufferFactory(ioAllocator, bufferSize);
	}
	
	/**
	 * data eclectical buffer factory
	 * @return
	 */
	protected DataBufferFactory buildDataBufferFactory() {
		return new DefaultDataBufferFactory(AbstractDataTransport.PROTOCOL_LENGTH, 4096, 8192 * 2);
	}
	
	/**
	 * create testing thread pool for test
	 * @param numThread
	 * @return
	 */
	protected ThreadPoolExecutor createTestExecutor(int numThread) {
		return new ThreadPoolExecutor(numThread, numThread, 0L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	/**
	 * transportation parameters configuration
	 * @return
	 */
	protected TransportConf buildTransportConf() {
		int revBuf = 1024 * 1024 * 64;
		return new DefaultTransportConf(4096, AbstractDataTransport.PROTOCOL_LENGTH, revBuf * 4, revBuf);
	}
	
	/**
	 * UDP transportation scheduler
	 * @return
	 */
	protected NotifiedSchedulerExecutor notifiedScheduler() {
		return SchedulerUtils.newScheduler(2, true);
	}
	
	/**
	 * transportation listener detect stream whether finish or repeat or error
	 * @return
	 */
	protected Supplier<TransportListener> transportListener(){
		return () -> new CleintTransportListener(saveDir + counter.getAndIncrement());
	}
	
	/**
	 * shutdown call back consumer
	 * @return
	 */
	protected Consumer<Executor> shutdownConsumer(){
		return (c) -> ((EventLoopGroup) c).shutdownGracefully();
	}
	
	/**
	 * worker supplier
	 * @return
	 */
	protected Supplier<Executor> workSupplier(){
		return () -> new NioEventLoopGroup(2);
	}
	
	/**
	 * stream protocol provider
	 * @param scheduler
	 * @param pdf
	 * @param factory
	 * @return
	 */
	protected Function<NioConfiguration, StreamProtocol> streamProtocol(NotifiedSchedulerExecutor scheduler,
			DataBufferFactory pdf, BufferFactory<ByteBuf> factory){
		return (c) -> {
			Map<String, NioBufferListener> listenerMap = new HashMap<>();
			IdempotentStreamProvider provider = new ClientIdempotentStreamProvider();
			//load system consumers
			DefaultClientConsumers<ByteBuf> consumers = new DefaultClientConsumers<>(pdf,c.getContext(),c.getTransport(),c.getUdpTransport(),provider,factory);
			consumers.setScheduler(scheduler);
			consumers.setExecutor(c.getWorkerExecutor());
			consumers.setConf(c.getClientConf());
			consumers.setListener(errorListener());
			BaseCodecFactory bcfactory = new BaseCodecFactory();
			listenerMap.putAll(ReflectionUtil.scan(consumers, bcfactory,factory,pdf));
			//load custom consumers
			listenerMap.putAll(loadCustomConsumers(bcfactory,factory,pdf));
			printConsumers(listenerMap);
			return new DefaultStreamProtocol<>(new DefaultListenerProtocol(c.getContext(),listenerMap));
		};
	}
	
	/**
	 * load custom consumers
	 * @param bcfactory
	 * @param factory
	 * @param dbf
	 * @return
	 */
	protected Map<String, NioAnnotationListener<ByteBuf>> loadCustomConsumers(BaseCodecFactory bcfactory,
			BufferFactory<ByteBuf> factory,DataBufferFactory dbf){
		MyClientConsumers myConsumers = new MyClientConsumers(dbf);
		return ReflectionUtil.scan(myConsumers, bcfactory,factory,dbf);
	}
	
	private void printConsumers(Map<String, NioBufferListener> map) {
		map.forEach((k,v)->logger.info("Consumer {} has been added",k));
	}
	
	/**
	 * error detector, if resource not found will print 404
	 * @return
	 */
	private TransportationListener errorListener() {
		return new TransportationListener(){
			@Override
			public void onError(long connectionId, String status, String resource, String msg) {
				System.err.println(String.format("Error Catched: %d %s %s %s",connectionId,status,resource,msg));
			}
		};
	}
	
	/**
	 * TCP data transporter
	 * @param conf
	 * @param pdf
	 * @param factory
	 * @return
	 */
	protected Function<NioConfiguration, DataTransport<? extends Protocol>> dataTransport(TransportConf conf, 
			DataBufferFactory pdf, BufferFactory<ByteBuf> factory){
		return (c) -> {
			DataProtocol<Meta> dp = new DefaulDataProtocol(c.getClientConf().getListener());
			AbstractDataTransport<ByteBuf, Meta> transport = new TCPDataTransport<>(c.getWorkerExecutor(),
					c.getContext(), conf, factory, pdf, dp);
			transport.setTransportListener(c.getClientConf().getListener());
			return transport;
		};
	}
	
	/**
	 * UDP data transporter
	 * @param conf
	 * @param factory
	 * @param pdf
	 * @param cFactory
	 * @param ioAllocator
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Function<NioConfiguration, UDPDataTransport<? extends Protocol>> udpDataTransport(TransportConf conf,
			BufferFactory<ByteBuf> factory, DataBufferFactory pdf, NioConnectionFactory<Channel> cFactory,
			ByteBufAllocator ioAllocator){
		Supplier<Integer> portFn = portSpl();
		return (c) -> {
			DataProtocol<Meta> dp = new UdpDataProtocol(c.getClientConf().getListener());
			try {
				InetSocketAddress socketAddress = new InetSocketAddress(localAddress.getAddress(), portFn.get());
				 AbstractDataTransport<ByteBuf, Meta> transport = new NettyUDPTransport<>(
						(EventLoopGroup) c.getWorkerExecutor(), c.getContext(), conf, 1000,
						socketAddress, false, factory, pdf, cFactory, dp, ioAllocator);
				transport.setTransportListener(c.getClientConf().getListener());
				return (UDPDataTransport<? extends Protocol>)transport;
			} catch (IOException e) {
				throw new NioException(e);
			}
		};
	}
	
	/**
	 * port supplier
	 * @return
	 */
	private Supplier<Integer> portSpl(){
		return ()->8002+(portCounter.getAndIncrement()%8000);
	}
	
	/**
	 * 21 file procession on each lap
	 * @param counertInteger
	 * @param pdf
	 * @param test
	 * @param c
	 * @return
	 */
	protected Runnable newRunnable(DataBufferFactory pdf,ClientPool<ByteBuf> test,int c) {
		return new Runnable() {
			@Override
			public void run() {
				NioSmartClient<ByteBuf> client = (NioSmartClient<ByteBuf>) test.borrowBuffer();
				try {
					testWriting(client, c, pdf);
					testReading(client);
					testWrongReading(client);
					testSending(client,c);
					//flush and await the lap of work complete
					client.awaitComplete(30000);
					
					runningCounter.incrementAndGet();
					test.release(client);
				} catch (Throwable e) {
					if (ConnectionException.class.isInstance(e)) {
						logger.error(e.getMessage());
					} else {
						logger.error(e.getMessage(), e);
					}
					test.dropObject(client);
				}
			}
		};
	}
	
	protected void testSending(NioSmartClient<ByteBuf> client, int num) throws IOException {
		client.send(PioUtil.msg("SAYHELLO", "John Consumer "+num, client.allcSendBuf(100)));
		client.send(PioUtil.msg("SAYHELLO2", null, client.allcSendBuf(100)));
	}
	
	protected void testWriting(NioSmartClient<ByteBuf> client, int num, DataBufferFactory pdf) throws IOException {
		int useUdp = 0;
		FileIdempotentStream stream = new FileIdempotentStream(num,TestUtil.getFile("/test.txt"), pdf);
		stream.setName("test-" + num + ".txt");
		client.write(stream, useUdp++%2==0);
	}
	
	protected void testReading(NioSmartClient<ByteBuf> client) throws IOException {
		client.read("image1/01-1.jpg", null, false);
		client.read("image1/01-2.jpg", null, false);
		client.read("image1/01-3.jpg", null, false);
		client.read("image1/02.jpg", null, false);
		client.read("image1/04.jpg", null, false);
		client.read("image1/06.jpg", null, false);
		client.read("image1/07.jpg", null, false);
		client.read("image1/08.jpg", null, false);
		client.read("image1/09.jpg", null, false);
		client.read("image1/10.jpg", null, false);
		client.read("image2/11.jpg", null, false);
		client.read("image2/12.jpg", null, false);
		client.read("image2/13.jpg", null, false);
		client.read("image2/14.jpg", null, false);
		client.read("image2/15.jpg", null, false);
		client.read("image2/16.jpg", null, false);
		client.read("image2/17.jpg", null, false);
		client.read("image2/18.jpg", null, false);
		client.read("image2/19.jpg", null, false);
		client.read("image2/20.jpg", null, false);
	}
	
	protected void testWrongReading(NioSmartClient<ByteBuf> client) throws IOException {
		client.read("image2/21.jpg", null, false);
	}
}
