package com.vision.niosmart.consumer;

public interface TransportationListener {
	void onError(long connectionId,String status,String resource,String msg);
}
