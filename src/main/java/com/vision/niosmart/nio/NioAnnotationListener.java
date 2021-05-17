package com.vision.niosmart.nio;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import com.vision.niosmart.consumer.BaseCodecFactory;
import com.vision.niosmart.consumer.Decoder;
import com.vision.niosmart.consumer.Parameter;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBuffer;
import com.vision.niosmart.stream.DataBufferFactory;
import com.vision.niosmart.util.NioBufferUtils;

public class NioAnnotationListener<B> implements NioBufferListener {
	
	private Object target;
	private Method method;
	private List<Parameter> params;
	private BaseCodecFactory factory;
	private BufferFactory<B> gbuffer;
	private DataBufferFactory dbf;
	

	public NioAnnotationListener(Object target, Method method, List<Parameter> params, 
			BaseCodecFactory factory,BufferFactory<B> gbuffer, DataBufferFactory dbf) {
		super();
		this.target = target;
		this.method = method;
		this.params = params;
		this.factory = factory;
		this.gbuffer = gbuffer;
		this.dbf = dbf;
	}
	
	@Override
	public boolean onBuffer(NioConnection s, GByteBuffer b) throws IOException {
		long id = s.getId();
		return onBuffer(id,s,b);
	}

	private boolean onBuffer(long id, NioConnection s, GByteBuffer b) throws IOException {
		Object[] args = new Object[params.size()];
		try {
			boolean truncate = false;
			for(int i=0;i<params.size();i++) {
				Parameter p = params.get(i);
				if(p.isId()) {
					args[i] = id;
				}else if(p.isSession()) {
					args[i] = s;
				}else if(p.isBuffer()) {
					args[i] = b;
				}else if(!truncate) {
					int len = p.getLength();
					len = len<=0?Integer.valueOf(args[Math.abs(len)].toString()):len;
					if(len<0) {
						throw new IOException("Cannot understand paremeter's lengh: "+len);
					}
					if(b.remaining()<len) {
						return false;
					}
					byte[] data = null;
					DataBuffer buf = null;
					if(len<20) {
						data = new byte[len];
						b.read(data);
					}else {
						buf = dbf.getDataBuffer();
						b.read(buf.get(), 0, len);
						data = buf.get();
					}
					try {
						Decoder decoder = factory.getCodec(p.getCodec());
						if(null==decoder) {
							throw new IOException("Decoder not found, "+p.getCodec());
						}
						Object object = decoder.decode(data,0,len);
						args[i] = object;
						if(object!=null&&object.toString().equals(p.getTruncate())) {
							truncate = true;
						}
					}finally {
						if(buf!=null) {
							buf.release();
						}
					}
				}else {
					args[i] = p.getDefaultValue();
				}
			}
			Object obj = method.invoke(target, args);
			if(GByteBuffer.class.isInstance(obj)) {
				GByteBuffer g = (GByteBuffer) obj;
				s.write(g.get());
				s.flush();
			}else if(DataBuffer.class.isInstance(obj)) {
				DataBuffer db = (DataBuffer) obj;
				NioBufferUtils.writeRetry(s, db, gbuffer);
				s.flush();
				db.release();
			}else if(Boolean.class.isInstance(obj)||boolean.class.isInstance(obj)) {
				return (boolean)obj;
			}
			return true;
		}catch(IOException e) {
			throw e;
		}catch(Throwable e) {
			throw new IOException(e);
		}
	}
}
