package com.vision.niosmart.nio.buffer;

import java.util.Iterator;
import java.util.LinkedList;

import com.vision.niosmart.exception.BufferException;
import com.vision.niosmart.nio.NioConnection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 * @author Jazping
 *
 */
public class NettyBuffer implements GByteBuffer {
	static UnpooledByteBufAllocator unPooled = new UnpooledByteBufAllocator(false);
	static PooledByteBufAllocator pooled = new PooledByteBufAllocator(false);
	protected LinkedList<ByteBuf> list = new LinkedList<>();
	protected int r = 0;
	protected int w = 0;
	private int markR = -1;
	private int keep = 4;
	
	public NettyBuffer(ByteBuf buf) {
		this(buf, false);
	}
	
	public NettyBuffer(ByteBuf buf,boolean clear) {
		this._add(buf, clear, true);
	}
	
//	public NettyBuffer(ByteBuf buf,boolean clear,int keeping) {
//		this(buf, clear);
//		this.keep = keeping;
//	}
	
	private synchronized GByteBuffer _add(ByteBuf buf,boolean clear, boolean first) {
		if(clear) {
			buf.clear();
		}
		if(first) {
			list.addFirst(buf);
		}else {
			list.add(buf);
		}
		this.w += buf.readableBytes();
		return this;
	}
	private static ThreadLocal<ByteBuf> local = new ThreadLocal<>();
	private synchronized ByteBuf getBuffer(int readCount) {
		int temp = readCount;
		ByteBuf tempBuffer = null;
		Iterator<ByteBuf> it = this.list.iterator();
		while(temp>0) {
			if(!it.hasNext()) {
				throw new java.nio.BufferUnderflowException();
			}
			ByteBuf buf = it.next();
			int rem = buf.readableBytes();
			if(rem<temp) {
				temp -= rem;
				if(rem>0) {
					if(readCount<512) {
						tempBuffer = tempBuffer==null?this.allocateTinyHeap(readCount):tempBuffer;
					}else {
						tempBuffer = tempBuffer==null?this.allocateNormalHeap(readCount):tempBuffer;
						local.set(tempBuffer);
					}
					tempBuffer.writeBytes(buf,rem);
				}
				if(this.list.size()>keep) {
					buf.release();
					it.remove();
					if(this.markR!=-1) {
						this.markR += rem;
					}
				}
			}else if(tempBuffer!=null){
				tempBuffer.writeBytes(buf, temp);
				temp = 0;
			}else {
				temp = 0;
				tempBuffer = buf;
			}
		}
		this.r += readCount;
		return tempBuffer;
	}
	
	protected ByteBuf allocateTinyHeap(int readCount) {
		return unPooled.buffer(readCount);
	}
	
	protected ByteBuf allocateNormalHeap(int readCount) {
		return pooled.buffer(readCount);
	}

	private synchronized void writeBuffer(byte[]data,int offset,int length) {
		int temp = length;
		int readPointer = 0;
		while(temp>0) {
			if(readPointer>=this.list.size()) {
				throw new java.nio.BufferUnderflowException();
			}
			ByteBuf buf = this.list.get(readPointer);
			int len = Math.min(buf.writableBytes(), temp);
			buf.writeBytes(data,offset,len);
			temp-=len;
			offset+=len;
			if(len==0||buf.writableBytes()==0) {
				readPointer++;
			}
			w+=len;
		}
	}

	@Override
	public int readInt() {
		return this.getBuffer(4).readInt();
	}

	@Override
	public long readLong() {
		return this.getBuffer(8).readLong();
	}

	@Override
	public short readShort() {
		return this.getBuffer(2).readShort();
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
	public int readPosition() {
		return r;
	}
	
	@Override
	public void markRead() {
		this.markR = r;
		this.list.forEach((b)->b.markReaderIndex());
	}

	@Override
	public void resetRead() {
		if(this.markR==-1) {
			throw new IllegalStateException("not mark yet");
		}
		this.list.forEach((b)->b.resetReaderIndex());
		this.r = markR;
		this.markR = -1;
	}
	
	@Override
	public void release() {
		this.list.forEach((b)->b.release());
		this.list.clear();
		this.r = 0;
		this.w= 0;
		this.markR = -1;
	}

	@Override
	public void read(byte[] dest, int offset, int length) {
		this.getBuffer(length).readBytes(dest, offset, length);
		ByteBuf rB = local.get();
		if(rB!=null) {
			rB.release();
			local.set(null);
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
		byte[] value = v.getBytes();
		this.writeShort(value.length);
		this.writeBuffer(value,0,value.length);
		return this;
	}

	@Override
	public GByteBuffer write(byte[] b, int offset, int length) {
		this.writeBuffer(b, offset, length);
		return this;
	}

	@Override
	public void writeTo(NioConnection s) {
		this.list.forEach((b)->s.write(b));
		this.list.clear();
	}

	@Override
	public Object get() {
		return this.list.getFirst();
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
		if(counter>15) {
			throw new BufferException("buffer can not digestion");
		}
		return this;
	}

	@Override
	public synchronized Object pollLast() {
		return this.list.pollLast();
	}

	@Override
	public GByteBuffer add(Object object, boolean clear, boolean first) {
		return this._add((ByteBuf)object, clear, first);
	}

	@Override
	public int getBufferCount() {
		return this.list.size();
	}

	@Override
	public Object dropFirst() {
		ByteBuf buf = this.list.pollFirst();
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		this.r += data.length;
		return  data;
	}

}
