package com.vision.niosmart;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * ConcurrentHashMap moded map entry factory
 * @author Jazping
 *
 * @param <K>
 * @param <V>
 */
public class ConcurrentModMapEntryFactory<K extends BigIntegerHashCode,V> implements ModedMapEntryFactory<K,V>{

	@Override
	public Map<K, V> newMap() {
		return new ConcurrentHashMap<>();
	}

}
