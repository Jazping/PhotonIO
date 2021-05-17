package com.vision.niosmart.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vision.niosmart.nio.NioExceptionHandler;

public class GenericNioExceptionHandler implements NioExceptionHandler {
	private Map<String,HandleValue> map = new LinkedHashMap<>();
	private boolean forceRunOver = true;
	
	@Override
	public void onThrow(Throwable throwable) {
		map.forEach((k,v)->{
			try {
				v.onThrow(throwable);
			}catch(RuntimeException e) {
				if(!forceRunOver) {
					throw e;
				}
			}
		});
	}
	
	public void setHandle(String name,Class<? extends Throwable> type, NioExceptionHandler handle) {
		map.put(name, new HandleValue(type,handle));
	}
	
	public void removeHandle(String name) {
		map.remove(name);
	}
	
	public void setAll(Map<String,HandleValue> map) {
		this.map.putAll(map);
	}
	
	public static class HandleValue{
		Class<? extends Throwable> type;
		NioExceptionHandler handle;
		
		public HandleValue(Class<? extends Throwable> type, NioExceptionHandler handle) {
			super();
			this.type = type;
			this.handle = handle;
		}
		
		public void onThrow(Throwable throwable) {
			if(type.isInstance(throwable)) {
				handle.onThrow(throwable);
			}
		}
	}

}
