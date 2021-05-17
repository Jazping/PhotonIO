package com.vision.niosmart.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vision.niosmart.BigIntegerHashCode;
import com.vision.niosmart.ModedMapEntryFactory;
import com.vision.niosmart.stream.IdempotentStreamWrapper;

class StreamModedMapEntryFactory implements ModedMapEntryFactory<BigIntegerHashCode,IdempotentStreamWrapper>{

	@Override
	public Map<BigIntegerHashCode, IdempotentStreamWrapper> newMap() {
		return new ConcurrentHashMap<>();
	}

}
