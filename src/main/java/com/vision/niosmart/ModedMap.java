package com.vision.niosmart;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * a slow down map key conflic by mod ModedMap, in some time the differen string hash code is the same one.
 * so use moded map to slow down that problem. be note that, this way does not restrict the crash ocurr.
 * <pre>
 * 1024 mod supported 1000000 map entry, but performen is down. 1024+ shuld be consider other cache component
 * 512 mod supported 100000 map entry
 * </pre>
 * @author Jazping
 *
 * @param <K> BigIntegerHashCode Key Type
 * @param <V> Value Type
 * 
 * @see ModedMapEntryFactory
 */
public class ModedMap<K extends BigIntegerHashCode,V> {
	protected List<Map<K,V>> mapEntries;
	protected BigInteger mod;
	protected ModedMapEntryFactory<K,V> factory;
	
	public ModedMap(int shared,ModedMapEntryFactory<K,V> factory) {
		if(shared<256) {
			throw new IllegalArgumentException("the shared map count min value 256");
		}
		mapEntries = new ArrayList<>(shared);
		this.setFactory(factory);
		this.mod = BigInteger.valueOf(shared);
		for(int i=0;i<shared;i++) {
			mapEntries.add(factory.newMap());
		}
	}
	
	public void clear() {
		mapEntries.forEach((m)->m.clear());
	}
	
	public boolean isEmpty() {
		boolean isEmpty = true;
		for(Map<K,V> m : mapEntries) {
			isEmpty = isEmpty&&m.isEmpty();
		}
		return isEmpty;
	}
	
	public V put(K key, V value) {
		int m = key.sharedCode();
		return mapEntries.get(m).put(key, value);
	}
	
	public V remove(K key) {
		int m = key.sharedCode();
		return mapEntries.get(m).remove(key);
	}
	
	public V get(K key) {
		int m = key.sharedCode();
		return mapEntries.get(m).get(key);
	}
	
	public boolean exists(K key) {
		int m = key.sharedCode();
		Map<K, V> map = mapEntries.get(m);
		return map!=null&&map.get(key)!=null;
	}
	
	public long size() {
		long size = 0;
		for(Map<K,V> m : mapEntries) {
			size+=m.size();
		}
		return size;
	}
	
	public void forEachEntry(BiConsumer<K, V> action) {
		mapEntries.forEach((m)->m.forEach(action));
	}
	
	public void forEach(Consumer<Map<K,V>> action) {
		mapEntries.forEach((m)->action.accept(m));
	}
	
	public ModedMapEntryFactory<K,V> getFactory() {
		return factory;
	}

	public void setFactory(ModedMapEntryFactory<K,V> factory) {
		this.factory = factory;
	}
}
