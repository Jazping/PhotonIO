package com.vision.niosmart.nio.buffer;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.nio.NioConnection;
/**
 * composited generic nio buffer, writeTo method to output, it will drop and release element after readed them.
 * it will try the best to avoid copy 512+ bytes, even ocurr, using pooled heap buffer to avoid copy between jvm and direct
 * <br>
 * <b>note:</b> must keep enough element (general required 2) for reading data that are breaked out.  
 * <b>note:</b> if keepping is to mush, that will be terrible for performance
 * @author Jazping
 *
 * @param <B> shell buffer type
 * @see MinaBuffer
 */
public abstract class AbstractGByteBuffer<B> implements GByteBuffer{
	protected LinkedList<B> list = new LinkedList<B>();
	protected int r = 0;
	protected int w = 0;
	protected int markR = -1;
	protected int keep = 4;
	protected AbstractGByteBuffer(){}
	protected AbstractGByteBuffer(B b,boolean clear) {
		this.add(b, clear,false);
	}
	
	protected AbstractGByteBuffer(B b,boolean clear, int keep) {
		this.add(b, clear,false);
		this.keep = keep;
	}
	
	private synchronized GByteBuffer _add(B buf,boolean clear, boolean first) {
		ByteBuffer b = this.nioBuffer(buf);
		if(clear) {
			b.clear();
			b.limit(0);
		}
		if(first) {
			list.addFirst(buf);
		}else {
			list.add(buf);
		}
		this.w += b.remaining();
		return this;
	}
	private static ThreadLocal<Object> local = new ThreadLocal<>();
	private synchronized B getBuffer(int readCount) {
		if(readCount<=0) {
			throw new IllegalStateException("read "+readCount+" bytes");
		}
		int temp = readCount;
		B tempBuffer = null;
		Iterator<B> it = this.list.iterator();
		while(temp>0) {
			if(!it.hasNext()) {
				throw new java.nio.BufferUnderflowException();
			}
			B buf = it.next();
			ByteBuffer b = nioBuffer(buf);
			int rem = b.remaining();
			if(rem<temp) {
				temp -= rem;
				if(rem>0) {
					if(readCount<512) {
						tempBuffer = tempBuffer==null?this.allocateTinyHeap(readCount):tempBuffer;
					}else {
						tempBuffer = tempBuffer==null?this.allocateNormalHeap(readCount):tempBuffer;
						local.set(tempBuffer);
					}
					nioBuffer(tempBuffer).put(b);
				}
				if(this.list.size()>keep) {
					release(buf);
					it.remove();
					if(this.markR!=-1) {
						this.markR += rem;
					}
				}
			}else if(tempBuffer!=null){
				ByteBuffer tempb = nioBuffer(tempBuffer);
				while(temp-->0) {
					tempb.put(b.get());
				}
				tempb.flip();
			}else {
				temp = 0;
				tempBuffer = buf;
			}
		}
		this.r += readCount;
		return tempBuffer;
	}
	
	private synchronized void writeBuffer(byte[]data,int offset,int length) {
		int temp = length;
		int readPointer = 0;
		while(temp>0) {
			if(readPointer>=this.list.size()) {
				throw new java.nio.BufferUnderflowException();
			}
			B buf = this.list.get(readPointer);
			ByteBuffer b = nioBuffer(buf);
			int read = b.position();
			int lim = b.limit();
			int len = Math.min(b.capacity() - b.limit(), temp);
			b.position(b.limit());
			b.limit(b.capacity());
			b.put(data, offset, len);
			temp-=len;
			offset+=len;
			if(len==0||len>b.capacity()-b.position()) {
				readPointer++;
			}
			b.position(read);
			b.limit(lim+len);
			w+=len;
		}
	}
	
	@Override
	public int readInt() {
		return nioBuffer(getBuffer(4)).getInt();
	}

	@Override
	public long readLong() {
		return nioBuffer(getBuffer(8)).getLong();
	}

	@Override
	public short readShort() {
		return nioBuffer(getBuffer(2)).getShort();
	}

	@Override
	public void read(byte[] dest) {
		this.read(dest, 0, dest.length);
	}

	@Override
	public int remaining() {
		return this.w - this.r;
	}

	@Override
	public boolean hasRemaining() {
		return remaining()>0;
	}
	
	@Override
	public GByteBuffer extend(GByteBuffer g) {
		Object o = g.pollLast();
		int counter = 0;
		while(o != null) {
			this.add(o, false,true);
			counter++;
			o = g.pollLast();
		}
		if(counter>5) {
			throw new BufferException("buffer can not digestion");
		}
		return this;
	}
	
	@Override
	public void markRead() {
		this.markR = r;
		this.list.forEach((b)->nioBuffer(b).mark());
	}
	@Override
	public void resetRead() {
		if(this.markR==-1) {
			throw new IllegalStateException("not mark yet");
		}
		this.list.forEach((b)->nioBuffer(b).reset());
		this.r = markR;
		this.markR = -1;
	}
	@Override
	public int readPosition() {
		return this.r;
	}
	@Override
	public GByteBuffer add(Object object,boolean clear,boolean first) {
		return this._add(castBuffer(object),clear,first);
	}
	@Override
	public void release() {
		this.list.forEach((b)->release(b));
		this.list.clear();
		this.r = 0;
		this.w= 0;
	}
	@Override
	public void read(byte[] dest, int offset, int length) {
		B b = this.getBuffer(length);
		ByteBuffer buf = this.nioBuffer(b);
		buf.get(dest, offset, length);
		@SuppressWarnings("unchecked")
		B rB = (B) local.get();
		if(rB!=null) {
			this.release(rB);
			local.remove();
		}
	}
	@Override
	public GByteBuffer writeInt(int v) {
		byte[] data = new byte[4];
		data[0] = (byte)(v >>> 24);
		data[1] = (byte)(v >>> 16);
		data[2] = (byte)(v >>> 8);
		data[3] = (byte)(v >>> 0);
		this.writeBuffer(data,0,4);
		return this;
	}
	@Override
	public GByteBuffer writeShort(int v) {
		byte[] data = new byte[2];
		data[0] = (byte)(v >>> 8);
		data[1] = (byte)(v >>> 0);
		this.writeBuffer(data,0,2);
		return this;
	}
	@Override
	public GByteBuffer write(byte[] b,int offset,int length) {
		this.writeBuffer(b,offset,length);
		return this;
	}
	
	@Override
	public GByteBuffer writeLong(long v) {
		byte[] data = new byte[8];
		data[0] = (byte)(v >>> 56);
		data[1] = (byte)(v >>> 48);
		data[2] = (byte)(v >>> 40);
		data[3] = (byte)(v >>> 32);
		data[4] = (byte)(v >>> 24);
		data[5] = (byte)(v >>> 16);
		data[6] = (byte)(v >>> 8);
		data[7] = (byte)(v >>> 0);
		this.writeBuffer(data,0,8);
		return this;
	}
	@Override
	public GByteBuffer writeString(String v) {
		byte[] d = v.getBytes();
		this.writeShort(d.length);
		this.writeBuffer(d, 0, d.length);
		return this;
	}
	@Override
	public void writeTo(NioConnection s) {
		this.list.forEach((b)->s.write(b));
	}
	@Override
	public Object get() {
		return this.list.getFirst();
	}
	
	public synchronized B pollLast(){
		return this.list.pollLast();
	}
	
	@Override
	public Object dropFirst() {
		return this.list.pollFirst();
	}
	
	@Override
	public int getBufferCount() {
		return this.list.size();
	}
	
//	@Override
//	public void drop() {
//		this.list.forEach((b)->nioBuffer(b).clear());
//		this.list.clear();
//	}
	
	/**
	 * release pooled buffer
	 * @param b
	 */
	protected abstract void release(B b);
	/**
	 * return nio native buffer of this buffer
	 * @param b
	 * @return
	 */
	protected abstract ByteBuffer nioBuffer(B b);
	/**
	 * allocate tiny unpooled buffer
	 * @param length
	 * @return
	 */
	protected abstract B allocateTinyHeap(int length);
	/**
	 * allocate normal pooled buffer
	 * @param length
	 * @return
	 */
	protected abstract B allocateNormalHeap(int length);
	/**
	 * cast shell buffer
	 * @param b
	 * @return
	 */
	protected abstract B castBuffer(Object b);
}
