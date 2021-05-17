package com.vision.niosmart.stream;

import java.io.IOException;
import java.util.Map;

public interface IdempotentStreamProvider {
	IdempotentStream getIdempotentStream(String resource,Map<String, String> headers) throws IOException;
}
