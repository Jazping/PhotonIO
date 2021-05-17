package com.vision.niosmart.stream;

import java.io.IOException;

@FunctionalInterface
public interface IOExceptionFunction<T, R>{
	 R apply(T t)throws IOException;
}
