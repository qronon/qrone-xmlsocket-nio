package org.qrone.xmlsocket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.qrone.xmlsocket.XMLSocket;
import org.qrone.xmlsocket.XMLSocketThread;

public abstract class SelectorServerSocket implements ExceptionListener {
	protected XMLSocketThread thread;
	protected ServerSocketChannel serverchannel;

	public SelectorServerSocket(XMLSocketThread thread) {
		this.thread = thread;
	}

	public void open(int port){
		try{
			serverchannel = ServerSocketChannel.open();
			InetSocketAddress isa = new InetSocketAddress(port);
			serverchannel.socket().bind(isa, 100);
	
			thread.register(serverchannel, SelectionKey.OP_ACCEPT, this);
			onOpen(true);
		}catch(IOException e){
			onOpen(false);
			onError(e);
		}
	}
	
	public void close(){
		try {
			serverchannel.close();
		} catch (IOException e) {}
		onClose();
	}

	public void accept(){
		try{
			SocketChannel channel = serverchannel.accept();
			if (channel != null) {
				XMLSocket socket = new XMLSocket(thread);
				socket.connect(channel);
				onNewClient(socket);
				socket.onConnect(true);
			}
		} catch (IOException e){
			close();
			onError(e);
		}
	}

	public ServerSocketChannel getServerSocketChannel() {
		return serverchannel;
	}
	
	public abstract void onOpen(boolean success);
	
	public abstract void onClose();
	
	public abstract void onNewClient(XMLSocket socket);

	public abstract void onError(Exception e);
}
