package com.vision.niosmart.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vision.niosmart.consumer.BaseCodecFactory;
import com.vision.niosmart.consumer.Consumer;
import com.vision.niosmart.consumer.Decoder;
import com.vision.niosmart.consumer.ID;
import com.vision.niosmart.consumer.Param;
import com.vision.niosmart.consumer.Parameter;
import com.vision.niosmart.nio.NioAnnotationListener;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.NioException;
import com.vision.niosmart.nio.buffer.BufferFactory;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.DataBufferFactory;

public class ReflectionUtil {
	
	public static <B> Map<String,NioAnnotationListener<B>> scan(Object target,BaseCodecFactory factory,
			BufferFactory<B> gbuffer,DataBufferFactory dbf){
		List<Method> list = findMethods(target.getClass(), Consumer.class, false,null);
		Map<String,NioAnnotationListener<B>> map = new HashMap<>();
		for(Method m : list) {
			m.setAccessible(true);
			Consumer c = m.getDeclaredAnnotation(Consumer.class);
			if(c.value().trim()=="") {
				throw new NioException("empty consumer mapping");
			}else if(c.value().trim().length()>20) {
				throw new NioException("consumer mapping length over 20");
			}
			List<Parameter> params = getMethodParameters(m, factory);
			NioAnnotationListener<B> l = new NioAnnotationListener<>(target, m, params, factory,gbuffer,dbf);
			map.put(c.value().trim(), l);
		}
		return map;
	}
	
	public static List<Method> findMethods(Class<?> cls, Class<? extends Annotation> annot, boolean accessible, List<Method> overrides){
		Method[] methods = cls.getDeclaredMethods();
		List<Method> list = new ArrayList<>();
		for(Method method : methods) {
			if( method.isAnnotationPresent(annot)) {
				int mod = method.getModifiers();
				if(accessible||(Modifier.isPublic(mod)||Modifier.isProtected(mod))) {
					if(null!=overrides) {
						boolean same = false;
						for(Method m : overrides) {
							if(equalMethod(m,method)) {
								same = true;
								break;
							}
						}
						if(!same) {
							list.add(method);
						}
					}else {
						list.add(method);
					}
				}
			}
		}
		if(cls!=Object.class) {
			list.addAll(findMethods(cls.getSuperclass(),annot,false,list));
		}
		return list;
	}
	
	private static boolean equalMethod(Method subMethod,Method method) {
		if(subMethod==null&&method==null) {
			return true;
		}else if(subMethod==null||method==null) {
			return false;
		}
		Class<?> c1 = subMethod.getDeclaringClass();
		Class<?> c2 = method.getDeclaringClass();
		return c2.isAssignableFrom(c1)&&subMethod.getName().equals(method.getName())&&equalParamTypes(subMethod.getParameterTypes(),method.getParameterTypes());
	}
	
	private static boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }
	
	public static List<Parameter> getMethodParameters(Method method,BaseCodecFactory factory){
		Consumer c = method.getDeclaredAnnotation(Consumer.class);
		if(c==null) {
			throw new IllegalArgumentException("Illegal consumer method");
		}
		Class<?>[] ts = method.getParameterTypes();
		List<Parameter> list = new ArrayList<>(ts.length);
		java.lang.reflect.Parameter[] ps = method.getParameters();
		for(int i=0;i<ts.length;i++) {
			Class<?> cls = ts[i];
			Parameter p = new Parameter();
			Param param = ps[i].getDeclaredAnnotation(Param.class);
			if(ps[i].getDeclaredAnnotation(ID.class)!=null) {
				p.setId(true);
			}else if(NioConnection.class.equals(cls)) {
				p.setSession(true);
			}else if(GByteBuffer.class.equals(cls)) {
				p.setBuffer(true);
			}
			Class<? extends Decoder> clz = BaseCodecFactory.getBaseDecoder(cls);
			if(clz==null&&param!=null) {
				clz = param.codec();
			}
			p.setCodec(clz);
			Decoder decoder = factory.getCodec(clz);
			if(decoder!=null&&!decoder.isFixLength()&&param==null) {
				throw new IllegalStateException("the "+i+"Parameter required fixed length");
			}
			if(decoder!=null) {
				int len = decoder.isFixLength()?decoder.getFixLength():param.value();
				p.setLength(len);
				p.setDefaultValue(decoder.defaultValue());
			}
			p.setType(cls);
			if(param!=null) {
				p.setTruncate(param.truncate());
			}
			list.add(p);
		}
		return list;
	}
}
