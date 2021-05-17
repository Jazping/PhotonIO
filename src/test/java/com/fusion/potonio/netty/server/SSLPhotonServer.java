package com.fusion.potonio.netty.server;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import com.fusion.potonio.netty.TestUtil;
import com.vision.niosmart.nio.NioConfiguration;

import io.netty.handler.ssl.SslContextBuilder;

/**
 * SSL PhotonIO server, only be connect by corresponding SSL PhotonIO client
 * @author Jazping
 *
 */
public class SSLPhotonServer extends PhotonServer{

	public SSLPhotonServer(String saveDir, String localIp) {
		super(saveDir, localIp);
	}
	
	public static void main(String[] args) {
		new SSLPhotonServer("C:\\Users\\Admin\\Desktop\\rev","192.168.1.100").start();
	}
	
	protected void afterConfBuild(NioConfiguration nioConf) throws IOException {
		String storeFile = "teststore";
		String storePass = "123456";
		String keyPass = storePass;
		String alias =  "key1";
		try {
			KeyStore keyStore = TestUtil.getStore("/"+storeFile,storePass,"JKS");
			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPass.toCharArray());
			X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
			SslContextBuilder contextBuilder = SslContextBuilder.forServer(privateKey, certificate);
			nioConf.setSslContext(contextBuilder.build());
		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			throw new IOException(e);
		} 
	}
}
