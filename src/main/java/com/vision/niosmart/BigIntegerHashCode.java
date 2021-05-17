package com.vision.niosmart;

import java.math.BigInteger;

public class BigIntegerHashCode {
	private int hashCode;
	private int sharedCode;
	
	public BigIntegerHashCode(byte[] hash,int mod) {
		BigInteger interger = new BigInteger(hash);
		this.hashCode = interger.hashCode();
		this.sharedCode = interger.abs().mod(BigInteger.valueOf(mod)).intValue();
	}
	
	public static BigIntegerHashCode valueOf(byte[] hash,int shared) {
		return new BigIntegerHashCode(hash,shared);
	}
	
	@Override
	public final int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "BigIntegerHashCode [hashCode=" + hashCode + ", sharedCode=" + sharedCode + "]";
//		return interger.toString();
	}

	@Override
	public final boolean equals(Object obj) {
		return obj instanceof BigIntegerHashCode && obj.hashCode() == this.hashCode;
	}

	public final int sharedCode() {
		return sharedCode;
	}
}
