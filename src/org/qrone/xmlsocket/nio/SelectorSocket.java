package org.qrone.xmlsocket.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public abstract class SelectorSocket implements ExceptionListener{
	private SelectorThread thread;
	private SocketChannel channel;
	
	private ByteBuffer inBuffer;
	private ByteBuffer outBuffer;
	private LinkedList sendqueue = new LinkedList();
	private ByteArrayOutputStream buf;

	private ProtocolEncoder encoder;
	private ProtocolDecoder decoder;
	
	private long lastAccess = System.currentTimeMillis();
	private long timeoutAccess = System.currentTimeMillis();
	
	public SelectorSocket(SelectorThread thread, 
			ProtocolEncoder encoder, 
			ProtocolDecoder decoder){
		this.thread = thread;
		this.encoder = encoder;
		this.decoder = decoder;
	}
	
	public void connect(String address, int port) throws IOException{
		connect(new InetSocketAddress(address,port));
	}
	
	public void connect(SocketAddress address) throws IOException{
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(address);
		
		thread.register(channel,SelectionKey.OP_CONNECT,this);
	}
	
	public void connect(SocketChannel channel){
		try {
			this.channel = channel;
			channel.socket().setReceiveBufferSize(1024);
			channel.socket().setSendBufferSize(1024);
	
			inBuffer = ByteBuffer.allocateDirect(channel.socket()
					.getReceiveBufferSize());
			
			thread.register(channel,SelectionKey.OP_READ,this);
		} catch (IOException e){
			close();
			onError(e);
		}
	}
	
	public void checkTimeout(long now){
		if(now - lastAccess > 60 * 1000
				&& now - timeoutAccess > 60 * 1000){
			timeoutAccess = System.currentTimeMillis();
			onTimeout();
		}
	}
	
	public void read() {
		lastAccess = System.currentTimeMillis();
		try{
			int readBytes = channel.read(inBuffer);
			if (readBytes == -1) {
				close();
				return;
			}else if (readBytes == 0) {
				return;
			}
			
			inBuffer.flip();
			byte[] packet = decoder.decode(inBuffer);
			while(packet != null){
				onPacket(packet);
				packet = decoder.decode(inBuffer);
			}
			inBuffer.clear();
		} catch (IOException e){
			close();
			onError(e);
		}
	}
	
	public boolean write(){
		try{
			if(outBuffer == null){
				synchronized (sendqueue) {
					if(sendqueue.size()>0){
						outBuffer = (ByteBuffer)sendqueue.remove(0);
					}
				}
			}
			
			channel.write(outBuffer);
			if (outBuffer.hasRemaining()){
				return true;
			} else {
				outBuffer = null;
				synchronized (sendqueue) {
					if(sendqueue.size()>0){
						return true;
					}
				}
			}
		} catch (IOException e){
			close();
			onError(e);
		}
		return false;
	}
	
	public SocketChannel getSocketChannel(){
		return channel;
	}
	
	public void send(byte[] packet){
		synchronized (sendqueue) {
			sendqueue.add(encoder.encode(packet));
		}
		thread.requestWrite(channel);
	}
	
	public void close(){
		try {
			channel.close();
		} catch (IOException e) {}
		onClose();
	}

	public abstract void onConnect(boolean success);
	
	public abstract void onClose();
	
	public abstract void onTimeout();
	
	public abstract void onPacket(byte[] b);
	
	public abstract void onError(Exception e);
}
