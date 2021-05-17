package com.fusion.potonio.netty.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.fusion.potonio.netty.TestUtil;
import com.vision.niosmart.client.Parameters;

import io.netty.buffer.ByteBuf;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * SSL PhotonIO client, only can connect to corresponding SSL PhotonIO server
 * @author Jazping
 *
 */
public class SSLPhotonClient extends PhotonClient{

	public SSLPhotonClient(InetSocketAddress remoteAddress, InetSocketAddress localAddress,String saveDir) {
		super(remoteAddress, localAddress, saveDir);
	}

	protected void afterParameterSet(Parameters<ByteBuf> ps) throws IOException {
		try {
			SslContextBuilder contextBuilder = SslContextBuilder.forClient();
			contextBuilder.trustManager((X509Certificate)TestUtil.loadX509Certificate("/testcer.cer"));
			ps.setSslContext(contextBuilder.build());
		}catch(CertificateException e) {
			throw new IOException(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		InetSocketAddress remoteAddress = new InetSocketAddress("192.168.1.100", 8001);
		InetSocketAddress localAddress = new InetSocketAddress("192.168.1.100", 0);
		String saveDir = "C:\\Users\\Admin\\Desktop\\cli\\c";
		new SSLPhotonClient(remoteAddress,localAddress,saveDir).start();
		
	}
}
