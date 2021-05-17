package com.vision.niosmart.protocol;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import com.vision.niosmart.Context;
import com.vision.niosmart.exception.StreamException;
import com.vision.niosmart.nio.NioBufferListener;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.StreamUtils;

public class DefaultListenerProtocol implements ListenerProtocol{
	private Context context;
	private Map<String, NioBufferListener> map;
	private Pattern p = Pattern.compile("[a-zA-Z0-9]+");
	public DefaultListenerProtocol(Context context,Map<String, NioBufferListener> map) {
		this.context = context;
		this.map = map;
	}
	
	public Map<String,NioBufferListener> getListenerMap() {
		return map;
	}

	public void setListenerMap(Map<String,NioBufferListener> map) {
		this.map = map;
	}

	@Override
	public int maxProtocolSize() {
		return 9+2;
	}

	@Override
	public NioBufferListener getListener(GByteBuffer b) throws IOException {
		if(null==map) {
			throw new StreamException("LISTENER MAP IS EMPTY");
		}
		byte[] data = StreamUtils.getUTF(b, 2,20);
		String head = new String(data,context.getCharset());
		if(map.containsKey(head)) {
			return map.get(head);
		}else if(p.matcher(head).matches()) {
			LOGGER.warn("Found protocol head '{}' but missing it's handler...",head);
		}
		return null;
	}
}
