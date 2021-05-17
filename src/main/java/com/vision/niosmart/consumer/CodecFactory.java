package com.vision.niosmart.consumer;

/**
 * Parameter decoder factory, fetch decoder by parameter annotation.
 * @author Jazping
 *
 */
public interface CodecFactory {
	/**
	 * decoder by parameter annotation
	 * @param cls the type of decoder
	 * @return return parameter decoder or null when no needed or not found
	 */
	Decoder getCodec(Class<? extends Decoder> cls);
}
