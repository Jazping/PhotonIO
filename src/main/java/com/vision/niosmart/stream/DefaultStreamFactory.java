package com.vision.niosmart.stream;


public class DefaultStreamFactory implements StreamFactory{
	
	private DataBufferFactory factory = new DefaultDataBufferFactory(1024*500, 1000);
	
	public DefaultStreamFactory() {
	}
	
	@Override
	public Stream newStream(Integer streamId, int segments, String name, int segmentSize, long length) {
		return new StreamImpl(streamId, segments, name,segmentSize,length,factory);
	}

	@Override
	public void clear() {
		
	}
	
}
