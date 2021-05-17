package com.vision.niosmart.nio;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.connection.Connection;
import com.vision.niosmart.connection.Connections;
import com.vision.niosmart.exception.ConnectionException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.stream.StreamUtils;
import com.vision.niosmart.transport.DataTransport;
import com.vision.niosmart.transport.UDPDataTransport;
import com.vision.niosmart.util.NioBufferUtils;

public class NioConnectListener<B> extends NioListener{
	private DataTransport<?> tcpt;
	private BufferFactory<B> gbuffer;
	private UDPDataTransport<?> udpt;
	
	public NioConnectListener(DataBufferFactory dbf,
			BufferFactory<B> gbuffer,
			Context context,DataTransport<?> tcpt, UDPDataTransport<?> udpt) {
		super(dbf,context);
		this.tcpt = tcpt;
		this.gbuffer = gbuffer;
		this.udpt = udpt;
	}

	@Override
	protected boolean onBuffer(long id, NioConnection session, GByteBuffer buffer) throws IOException {
		return super.headRequest(id, "CONNECT", buffer, (line,headers)->{
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
			
//			boolean required = tryShakeHands(session,headers);
			
//			if(headers.containsKey(NioConnection.DEFAULT_PUBLIC_KEY)) {
//				session.setAttribute(NioConnection.DEFAULT_PUBLIC_KEY, headers.get(NioConnection.DEFAULT_PUBLIC_KEY));
//			}
			
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
			con.setRemoteSegSize(Integer.valueOf(segStr));
			session.setAttribute(NioConnection.MAX_SEGMENT_SIZE, Integer.valueOf(segStr));
			if(headers.containsKey(NioConnection.UPORT)) {
				String tport = headers.get(NioConnection.UPORT).trim();
				con.setRemoteReceivePort(Integer.valueOf(tport));
				session.setAttribute(NioConnection.UPORT, Integer.valueOf(tport));
				InetSocketAddress socketAddress = session.getRemoteAddress();
				con.setUDPTransport(udpt,socketAddress.getAddress(), con.getRemoteReceivePort());
				new Thread() {
					public void run() {
						con.getUdpConnection(true);
					}
				}.start();
			}
			String bufStr = headers.get(NioConnection.REV_BUFFER).trim();
			con.setRemoteReceiveBuf(Integer.valueOf(bufStr));
			session.setAttribute(NioConnection.REV_BUFFER, Integer.valueOf(bufStr));
			Connections.putSession(nioId, session);
			DataBuffer db = dbf.getDataBuffer();
			try {
				StreamUtils.toBuffer("ESTABLISH", sb.toString().getBytes(), db);
				NioBufferUtils.writeRetry(session, db, gbuffer);
				session.flush();
				return true;
			}finally {
				db.release();
			}
		},false);
	}
	
//	private boolean tryShakeHands(NioConnection session,Map<String,String> headers) {
//		NioConfiguration conf = NioSmartServer.getConfiguration();
//		if(conf!=null) {
//			PrivateKey privateKey = conf.getPrivateKey();
//			if(privateKey!=null&&conf.isAuthorized()) {
//				if(!headers.containsKey(NioConnection.ENCRYPT_ATTESTATION)) {
//					session.close();
//					throw new ConnectionException("connection rejected for 'Authentication'");
//				}
//				String value = headers.get(NioConnection.ENCRYPT_ATTESTATION);
//				byte[] data = Base64.decodeBase64(value);
//				if(data.length!=256) {
//					session.close();
//					throw new ConnectionException("connection rejected for 'Authentication'");
//				}
//				try {
//					Cipher cipher = Cipher.getInstance("RSA");
//					cipher.init(Cipher.DECRYPT_MODE, privateKey);
//					cipher.doFinal(data);
//				} catch (IllegalBlockSizeException e) {
//					session.close();
//					throw new ConnectionException("connection rejected for 'Authentication'");
//				} catch (BadPaddingException e) {
//					session.close();
//					throw new ConnectionException("connection rejected for 'Authentication'");
//				} catch(Exception e) {
//					logger.error(e.getMessage(),e);
//					session.close();
//					throw new ConnectionException("connection rejected for 'ServerError'");
//				}
//				return true;
//			}
//		}
//		return false;
//	}
}
