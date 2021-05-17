package com.fusion.potonio.netty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.apache.commons.io.IOUtils;

public class TestUtil {
	private TestUtil() {}
	public static InputStream getStream(String path) {
		return TestUtil.class.getResourceAsStream(path);
	}
	
	public static File getFile(String path) {
		URL url = TestUtil.class.getResource(path);
		String file = url.getFile();
		return new File(file);
	}
	
	public static KeyStore getStore(String path,String storePass,String alog) throws IOException, KeyStoreException {
		InputStream stream = TestUtil.getStream(path);
		try {
			if(stream==null) {
				throw new KeyStoreException("Store File Not Found: "+path);
			}
			KeyStore store = KeyStore.getInstance(alog);
			store.load(stream, storePass.toCharArray());
			return store;
		} catch (NoSuchAlgorithmException e) {
			throw new KeyStoreException(e);
		} catch (CertificateException e) {
			throw new KeyStoreException(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}
	
	public static Certificate loadX509Certificate(String path) throws CertificateException {
		return loadCertificate(path,"X509");
	}
	
	public static Certificate loadCertificate(String path,String format) throws CertificateException {
		CertificateFactory factory = CertificateFactory.getInstance(format);
		InputStream input = TestUtil.getStream(path);
		if(input==null) {
			throw new CertificateException("certificate file not found");
		}
		try {
			return factory.generateCertificate(input);
		}finally {
			IOUtils.closeQuietly(input);
		}
	}
}
