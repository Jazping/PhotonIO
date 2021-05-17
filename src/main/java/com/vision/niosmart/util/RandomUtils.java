package com.vision.niosmart.util;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

public class RandomUtils {
	private static final SecureRandom SECURERANDOM = new SecureRandom();
	public static String randomBase64UrlSafe(String spi,int len) {
		if(spi==null) {
			throw new NullPointerException();
		}
		if(len<1) {
			throw new IllegalArgumentException();
		}
		byte[] random = new byte[len];
		SECURERANDOM.nextBytes(random);
		byte[] spiBytes = spi.getBytes();
		byte[] bytes = new byte[random.length+spiBytes.length];
		System.arraycopy(spiBytes, 0, bytes, 0, spiBytes.length);
		System.arraycopy(random, 0, bytes, spiBytes.length, random.length);
		return Base64.encodeBase64URLSafeString(bytes);
	}
	
	public static long randomPositiveLong(int maxLen) {
		if(maxLen>19) {
			throw new  IllegalArgumentException("no such long");
		}
		StringBuilder sb = new StringBuilder();
		int len = maxLen;
		while(len-->=0) {
			int i = SECURERANDOM.nextInt(10);
			while(i==0&&(len==maxLen-1||(len==0&&sb.charAt(maxLen-2)=='0'))) {
				i = SECURERANDOM.nextInt(10);
			}
			sb.append(String.valueOf(i));
		}
		return Long.valueOf(sb.toString());
	}
}
