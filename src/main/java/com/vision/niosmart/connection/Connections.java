package com.vision.niosmart.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vision.niosmart.nio.NioConnection;

public class Connections {
	private static Map<Long,NioConnection> sessionMap = new ConcurrentHashMap<>();
	
	public static void putSession(Long key,NioConnection session) {
		sessionMap.put(key, session);
	}
	public static void removeSession(Long key) {
		sessionMap.remove(key);
	}
	public static NioConnection getSession(Long key) {
		return sessionMap.get(key);
	}
}
