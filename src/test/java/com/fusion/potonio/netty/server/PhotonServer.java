package com.fusion.potonio.netty.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.niosmart.Context;
import com.vision.niosmart.DefaultContext;
import com.vision.niosmart.consumer.BaseCodecFactory;
import com.vision.niosmart.consumer.DefaultServerConsumers;
import com.vision.niosmart.nio.NioAnnotationListener;
import com.vision.niosmart.nio.NioBufferListener;
import com.vision.niosmart.nio.NioConfiguration;
import com.vision.niosmart.nio.NioConnectionFactory;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.NettyBufferFactory;
import com.vision.niosmart.nio.netty.NettyConnectionFactory;
import com.vision.niosmart.nio.netty.NettyFactory;
import com.vision.niosmart.nio.netty.NettyUDPTransport;
import com.vision.niosmart.protocol.DataProtocol;
import com.vision.niosmart.protocol.DefaulDataProtocol;
import com.vision.niosmart.protocol.DefaultListenerProtocol;
import com.vision.niosmart.protocol.DefaultStreamProtocol;
import com.vision.niosmart.protocol.ListenerProtocol;
import com.vision.niosmart.protocol.Meta;
import com.vision.niosmart.protocol.UdpDataProtocol;
import com.vision.niosmart.server.NioSmartServer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.DefaultDataBufferFactory;
import com.vision.niosmart.stream.IdempotentStreamProvider;
import com.vision.niosmart.transport.AbstractDataTransport;
import com.vision.niosmart.transport.DefaultTransportConf;
import com.vision.niosmart.transport.NotifiedSchedulerExecutor;
import com.vision.niosmart.transport.TCPDataTransport;
import com.vision.niosmart.transport.TransportConf;
import com.vision.niosmart.transport.TransportListener;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.ReflectionUtil;
import com.vision.niosmart.util.SchedulerUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * PhotonIO server example
 * @author Jazping
 *
 */
public class PhotonServer extends Thread{
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected String saveDir = null;
	protected String localIp = null;
	
	public PhotonServer(String saveDir,String localIp) {
		this.saveDir = saveDir;
		this.localIp = localIp;
		this.setName("PhotonIO-Demo");
	}

	public static void main(String[] args) {
		//need to change directory for saving and the IP which distributed by local network
		new PhotonServer("C:\\Users\\Admin\\Desktop\\rev\\fromclient","192.168.1.100").start();
	}
	
	public void run() {
		try {
			//create event loops
			EventLoopGroup workerGroup = buildWorkerGroup();
			//create pooled buffer allocator
			PooledByteBufAllocator ioAllocator = new PooledByteBufAllocator(true);
			NioConfiguration nioConf = buildConf(workerGroup,ioAllocator);
			//build transport configuration;
			TransportConf conf = buildTransportConf();
			//build buffer factory
			BufferFactory<ByteBuf> factory =  buildBufferFactory(ioAllocator);
			//build connection factory
			NioConnectionFactory<Channel> pFactory = new NettyConnectionFactory(conf);
			//create transport listener
			TransportListener listener = new MyServerTransportListener(saveDir);
			//create data protocol instance
			DataProtocol<Meta> dp = new DefaulDataProtocol(listener);
			DataProtocol<Meta> udp = new UdpDataProtocol(listener);
			//create high cache buffer factory
			DataBufferFactory dbf = new DefaultDataBufferFactory(conf.getProtocolSize(),conf.getMinRead(), 8192*2);
			//create TCP transportation
			AbstractDataTransport<ByteBuf,Meta> transport = createTransportor(workerGroup,nioConf.getContext(),conf,factory,dp,pFactory,dbf);
			transport.setTransportListener(listener);
			transport.setProtocol(dp);
			nioConf.setTransport(transport);
			//create UDP transportation
			AbstractDataTransport<ByteBuf,Meta> audpt = createUdpTransportor(workerGroup, nioConf.getContext(), 
					conf, 50, nioConf.getLocalAddress(), true, factory, udp, pFactory, dbf,ioAllocator);
			audpt.setTransportListener(listener);
			nioConf.setUdpTransport((UDPDataTransport<?>)audpt);
			UDPDataTransport<?> udpt = (UDPDataTransport<?>) audpt;
			Map<String,NioBufferListener> listenerMap = new HashMap<>();
			//create transportation scheduler
			NotifiedSchedulerExecutor scheduler = createScheduler();
			//create idempotent content stream provider
			IdempotentStreamProvider provider = new MyIdempotentStreamProvider();
			
			//scan system consumers
			DefaultServerConsumers<ByteBuf> consumers = new DefaultServerConsumers<>(dbf,nioConf.getContext(),transport,udpt,provider,factory);
			consumers.setScheduler(scheduler);
			consumers.setExecutor(workerGroup);
			BaseCodecFactory bcfactory = new BaseCodecFactory();
			listenerMap.putAll(ReflectionUtil.scan(consumers, bcfactory,factory,dbf));
			//scan custom consumers
			listenerMap.putAll(loadCustomConsumers(bcfactory,factory,dbf));
			printConsumers(listenerMap);
			
			//define stream protocol
			ListenerProtocol protocol = new DefaultListenerProtocol(nioConf.getContext(),listenerMap);
			nioConf.setProtocol(new DefaultStreamProtocol<>(protocol));
			
			//new server and bind to network
			NioSmartServer nioServer = new NioSmartServer(nioConf,new NettyFactory(factory,ioAllocator,pFactory));
			nioServer.bind();
		}catch(IOException e) {
			logger.error(e.getMessage(),e);
		}
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
		MyServerConsumers myConsumers = new MyServerConsumers(factory);
		return ReflectionUtil.scan(myConsumers, bcfactory,factory,dbf);
	}
	
	private void printConsumers(Map<String, NioBufferListener> map) {
		map.forEach((k,v)->logger.info("Consumer {} has been added",k));
	}
	
	/**
	 * create transportation scheduler for UDP
	 * @return
	 */
	protected NotifiedSchedulerExecutor createScheduler() {
		int t = Runtime.getRuntime().availableProcessors();
		return SchedulerUtils.newScheduler(t, true);
	}
	
	/**
	 * build configuration
	 * @param workerGroup
	 * @return
	 */
	protected NioConfiguration buildConf(EventLoopGroup workerGroup, ByteBufAllocator alloc) throws IOException{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
	    NioConfiguration nioConf = new NioConfiguration();
		nioConf.setPort(8001);
		DefaultContext context = new DefaultContext();
		nioConf.setContext(context);
		nioConf.setLocalAddress(new InetSocketAddress(localIp, nioConf.getPort()));
		nioConf.setBindAddress(nioConf.getLocalAddress());
		nioConf.setIoExecutor(bossGroup);
		nioConf.setWorkerExecutor(workerGroup);
		nioConf.setAlloc(alloc);
		afterConfBuild(nioConf);
		return nioConf;
	}
	
	/**
	 * will invoke after configuration builded, use for child class destine configuration
	 * @param context
	 * @throws IOException
	 */
	protected void afterConfBuild(NioConfiguration context) throws IOException{
		
	}
	
	/**
	 * build transport configuration
	 * @return
	 */
	protected TransportConf buildTransportConf() {
		int revBuf = 1024*1024*64;
		return new DefaultTransportConf(4096,AbstractDataTransport.PROTOCOL_LENGTH,revBuf,revBuf*4);
	}
	
	/**
	 * build buffer factory
	 * @param ioAllocator
	 * @return
	 */
	protected BufferFactory<ByteBuf> buildBufferFactory(ByteBufAllocator ioAllocator){
		int bufferSize = 4096+AbstractDataTransport.PROTOCOL_LENGTH;
		return new NettyBufferFactory(ioAllocator,bufferSize);
	}
	
	/**
	 * build worker thread group
	 * @return
	 */
	protected EventLoopGroup buildWorkerGroup() {
		return new NioEventLoopGroup();
	}
	
	/**
	 * create TCP tunnel transportation
	 * @param workerExecutor
	 * @param context
	 * @param conf
	 * @param factory
	 * @param dp
	 * @param pFactory
	 * @param dbf
	 * @return
	 * @throws IOException
	 */
	protected AbstractDataTransport<ByteBuf,Meta> createTransportor(Executor workerExecutor, Context context, 
			TransportConf conf,BufferFactory<ByteBuf> factory,DataProtocol<Meta> dp,
			NioConnectionFactory<Channel> pFactory,DataBufferFactory dbf) throws IOException {
		return new TCPDataTransport<ByteBuf,Meta>(workerExecutor, context, conf,factory,dbf,dp);
	}
	
	/**
	 * create UDP tunnel transportation
	 * @param workerExecutor
	 * @param context
	 * @param conf
	 * @param repeat
	 * @param receive
	 * @param b
	 * @param factory
	 * @param dp
	 * @param pFactory
	 * @param dbf
	 * @param allocator
	 * @return
	 * @throws IOException
	 */
	protected AbstractDataTransport<ByteBuf,Meta> createUdpTransportor(EventLoopGroup workerExecutor, Context context, 
			TransportConf conf,int repeat, InetSocketAddress receive, boolean b,BufferFactory<ByteBuf> factory,
			DataProtocol<Meta> dp,
			NioConnectionFactory<Channel> pFactory,
			DataBufferFactory dbf,ByteBufAllocator allocator) throws IOException {
		return new NettyUDPTransport<>(workerExecutor, context, conf,repeat,receive,b,factory,dbf,pFactory,dp,allocator);
	}

}
