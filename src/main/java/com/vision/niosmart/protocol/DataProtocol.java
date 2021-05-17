package com.vision.niosmart.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.vision.niosmart.Context;
import com.vision.niosmart.exception.ProtocolException;
import com.vision.niosmart.nio.NioConnection;
import com.vision.niosmart.nio.buffer.GByteBuffer;
import com.vision.niosmart.stream.Stream;
import com.vision.niosmart.transport.TransportIoHandler;
/**
 * protocol to reading or writting stream on NIO, dump protocol first, and validate current buffer whether correct
 * @author Jazping
 *
 * @param <E> using generic Protocol Entity to dump protocol
 * 
 * @see DefaulDataProtocol
 */
public interface DataProtocol<E extends Protocol> {
	/**
	 * dump protocol, if buffer out of bound, than throw ProtocolException
	 * @param id connection id
	 * @param fromAddress additional parameter
	 * @param buf the generic buffer
	 * @return generic Protocol
	 * @throws ProtocolException when buffer out of bound
	 */
	E dumpProtocol(long id, InetSocketAddress fromAddress,GByteBuffer buf)throws ProtocolException;
	/**
	 * return true if connection available
	 * @param ctx Context
	 * @param e generic protocol
	 * @return true if connection available and stream is not fully, false otherwise
	 */
	boolean isAvailable(Context ctx,E e);
	/**
	 * in udp protocol, when recive data send confirm report
	 * @param h TransportIoHandler
	 * @param s current connection
	 * @param ctx Context
	 * @param e generic protocol
	 */
	void beforeRecvice(TransportIoHandler<E> h,NioConnection s,Context ctx,E e) throws IOException;
	/**
	 * reall save the stream segment data
	 * @param ctx Context
	 * @param e generic protocol
	 * @param packet segment data with out protocol
	 * @param offset data offset
	 * @param length data length
	 * @return true when write successfull,false if duplicate and so on
	 */
	boolean recvice(Context ctx,E e,byte[] packet,int offset,int length);
	/**
	 * use to analyse repeat packet
	 * @param fromAddress additional parameter
	 * @param ctx Context
	 * @param e generic protocol
	 */
	void onDataRepeat(InetSocketAddress fromAddress,Context ctx,E e);
	/**
	 * return true if this stream is all segment recivced
	 * @param ctx Context
	 * @param e generic protocol
	 * @return return true if this stream is all segment recivced
	 */
	boolean isFully(Context ctx,E e);
	/**
	 * finish this stream when all segement have recivced
	 * @param ctx Context
	 * @param e generic protocol
	 * @return true if finish,false if duplicate invoke by concurrent thread
	 * @throws IOException 
	 */
	boolean finish(Context ctx,E e) throws IOException;
	/**
	 * when finish stream cleanup if need
	 * @param ctx Context
	 * @param e generic protocol
	 * @return current finished stream 
	 */
	Stream cleanUp(Context ctx,E e);
	/**
	 * build a private protocol on the transport segment buffer
	 * @param buf
	 * @param connectionId
	 * @param streamId
	 * @param segment
	 * @param pos
	 * @return 
	 * @throws IOException
	 */
	void buildDataBuffer(GByteBuffer buf,long connectionId,int streamId,int segment,long pos)throws IOException;
	/**
	 * transfer to destination where you want
	 * @param stream the finished stream
	 * @see Stream.saveTo
	 */
	void transfer(Stream stream);
}
