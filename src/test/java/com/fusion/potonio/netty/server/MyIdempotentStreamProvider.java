package com.fusion.potonio.netty.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;

import com.vision.niosmart.stream.ByteIdempotentStream;
import com.vision.niosmart.stream.IdempotentStream;
import com.vision.niosmart.stream.IdempotentStreamProvider;

/**
 * idempotent stream provider for test of server side
 * @author Jazping
 *
 */
public class MyIdempotentStreamProvider implements IdempotentStreamProvider {
	/**
	 * cached testing resource
	 */
	protected Map<String, IdempotentStream> cacheMap = new ConcurrentHashMap<>();
	
	@Override
	public IdempotentStream getIdempotentStream(String resource, Map<String, String> headers) throws IOException {
		if(!cacheMap.containsKey(resource)) {
			synchronized (cacheMap) {
				if(!cacheMap.containsKey(resource)) {
					String res = formatRes(resource);
					InputStream fileStream = MyIdempotentStreamProvider.class.getResourceAsStream(res);
					if(null!=fileStream) {
						try {
							byte[] data = IOUtils.toByteArray(fileStream);
							IdempotentStream stream = new ByteIdempotentStream(0, data, resource);
							cacheMap.put(resource, stream);
						}finally {
							IOUtils.closeQuietly(fileStream);
						}
					}
				}
			}
		}
		return cacheMap.get(resource);
	}
	
	protected String formatRes(String resource) {
		if(resource.startsWith("/")) {
			return resource;
		}
		return "/".concat(resource);
	}

}
