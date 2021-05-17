package com.vision.niosmart.consumer;

import java.util.HashMap;
import java.util.Map;

public class BaseCodecFactory implements CodecFactory{
	
	private static ByteDecoder byteD = new ByteDecoder();
	private static ShortDecoder shortD = new ShortDecoder();
	private static IntDecoder intD = new IntDecoder();
	private static FloatDecoder floatD = new FloatDecoder();
	private static LongDecoder longD = new LongDecoder();
	private static DoubleDecoder doubleD = new DoubleDecoder();
	private static StringDecoder stringD = new StringDecoder();
	private static BytesDecoder bytesD = new BytesDecoder();
	
	private static Map<Class<?>,Decoder> map = new HashMap<>();
	
	static {
		map.put(ByteDecoder.class, byteD);
		map.put(ShortDecoder.class, shortD);
		map.put(IntDecoder.class, intD);
		map.put(FloatDecoder.class, floatD);
		map.put(LongDecoder.class, longD);
		map.put(DoubleDecoder.class, doubleD);
		map.put(StringDecoder.class, stringD);
		map.put(BytesDecoder.class, bytesD);
	}
	
	private static Map<Class<?>,Class<? extends Decoder>> zmap = new HashMap<>();
	
	static {
		zmap.put(int.class, IntDecoder.class);
		zmap.put(Integer.class, IntDecoder.class);
		
		zmap.put(long.class, LongDecoder.class);
		zmap.put(Long.class, LongDecoder.class);
		
		zmap.put(float.class, FloatDecoder.class);
		zmap.put(Float.class, FloatDecoder.class);
		
		zmap.put(double.class, DoubleDecoder.class);
		zmap.put(Double.class, DoubleDecoder.class);
		
		zmap.put(short.class, ShortDecoder.class);
		zmap.put(Short.class, ShortDecoder.class);
		
		zmap.put(byte.class, ByteDecoder.class);
		zmap.put(Byte.class, ByteDecoder.class);
		
		zmap.put(char.class, ByteDecoder.class);
		
		zmap.put(String.class, StringDecoder.class);
		zmap.put(byte[].class, BytesDecoder.class);
	}
	
	private CodecFactory sencondFactory;
	
	@Override
	public Decoder getCodec(Class<? extends Decoder> cls) {
		Decoder d = map.get(cls);
		if(d==null&&sencondFactory!=null) {
			d = sencondFactory.getCodec(cls);
		}
		return d;
	}
	
	public static Class<? extends Decoder> getBaseDecoder(Class<?> cls){
		return zmap.get(cls);
	}

	public CodecFactory getSencondFactory() {
		return sencondFactory;
	}

	public void setSencondFactory(CodecFactory sencondFactory) {
		this.sencondFactory = sencondFactory;
	}
}
