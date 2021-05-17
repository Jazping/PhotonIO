package com.vision.niosmart.connection;

public class DefaultConnectionFactory implements ConnectionFactory {

	@Override
	public Connection newConnection(long id) {
		return new ConnectionImpl(id);
	}

}
