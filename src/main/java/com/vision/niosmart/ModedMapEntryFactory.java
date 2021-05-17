package com.vision.niosmart;

import java.util.Map;

/**
 * ModedMapEntryFactory for create map entry
 * @author Jazping
 *
 * @param <K>
 * @param <V>
 */
public interface ModedMapEntryFactory<K extends BigIntegerHashCode,V> {
	/**
	 * new Map instance
	 * @return
	 */
	Map<K,V> newMap();
}
