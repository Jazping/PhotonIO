package com.vision.niosmart.stream;

import java.io.IOException;

@FunctionalInterface
public interface IOExceptionConsumer<T> {
	 void accept(T t)throws IOException;
}
