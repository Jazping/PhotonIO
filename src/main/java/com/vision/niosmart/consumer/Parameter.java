package com.vision.niosmart.consumer;

public class Parameter {
	private int length;
	private String truncate = "";
	private Object defaultValue;
	private Class<? extends Decoder> codec;
	private Class<?> type;
	private boolean id;
	private boolean session;
	private boolean buffer;
	
	public Parameter() {
		
	}
	
	public Parameter(int length, Class<? extends Decoder> codec) {
		super();
		this.length = length;
		this.codec = codec;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public Class<? extends Decoder> getCodec() {
		return codec;
	}
	public void setCodec(Class<? extends Decoder> codec) {
		this.codec = codec;
	}
	public String getTruncate() {
		return truncate;
	}
	public void setTruncate(String truncate) {
		this.truncate = truncate;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Class<?> getType() {
		return type;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
	public boolean isId() {
		return id;
	}
	public void setId(boolean id) {
		this.id = id;
	}
	public boolean isSession() {
		return session;
	}
	public void setSession(boolean session) {
		this.session = session;
	}

	@Override
	public String toString() {
		return "Parameter [length=" + length + ", truncate=" + truncate + ", defaultValue=" + defaultValue + ", codec="
				+ codec + ", type=" + type + ", id=" + id + ", session=" + session + "]";
	}

	public boolean isBuffer() {
		return buffer;
	}

	public void setBuffer(boolean buffer) {
		this.buffer = buffer;
	}
	
	
}
