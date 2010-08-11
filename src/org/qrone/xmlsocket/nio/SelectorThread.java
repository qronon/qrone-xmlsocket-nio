package org.qrone.xmlsocket.nio;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;

public class SelectorThread {
	private static final Logger log = Logger.getLogger(SelectorThread.class);
	
	private ExceptionListener listener;
	private Selector selector;
	private Object timeoutWait = new Object();
	private boolean open;

	private LinkedList tasklist = new LinkedList();

	public SelectorThread() throws IOException {
		this(null);
	}
	
	public SelectorThread(ExceptionListener listener) throws IOException {
		this.listener = listener;
		selector = Selector.open();
		open = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					synchronized (tasklist) {
						for (Iterator iter = tasklist.iterator(); iter
								.hasNext();) {
							((Runnable) iter.next()).run();
						}
						tasklist.clear();
					}

					if (!open) {
						return;
					}

					try {
						int numOfKeys = selector.select(10 * 1000);
						
						// Timeout Check
						Set set = selector.keys();
						for (Iterator iter = set.iterator(); iter.hasNext();) {
							SelectionKey key = (SelectionKey) iter.next();
							Object o = key.attachment();
							if(o instanceof SelectorSocket){
								((SelectorSocket)o).checkTimeout(
										System.currentTimeMillis());
							}
						}
						if (numOfKeys == 0) {
							continue;
						}
					} catch (IOException e) {
						onError(e);
						continue;
					}

					Iterator it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();
						try {
							int readyOps = key.readyOps();
							key.interestOps(key.interestOps() & ~readyOps);

							if (key.isAcceptable()) {
								SelectorServerSocket s = (SelectorServerSocket) key
										.attachment();
								s.accept();
								key.interestOps(key.interestOps()
										| SelectionKey.OP_ACCEPT);

							} else if (key.isConnectable()) {
								SelectorSocket s = (SelectorSocket) key
										.attachment();
								try {
									if (!s.getSocketChannel().finishConnect()) {
										s.onConnect(false);
									} else {
										s.connect(s.getSocketChannel());
										s.onConnect(true);
									}
								} catch (IOException e) {
									s.onError(e);
									s.onConnect(false);
								}

							} else if(key.isReadable() || key.isWritable()){
								SelectorSocket s = (SelectorSocket) key
										.attachment();
								if (key.isReadable()) {
									s.read();
									key.interestOps(key.interestOps()
											| SelectionKey.OP_READ);
								}
								if (key.isValid() && key.isWritable()) {
									boolean writemore = s.write();
									if (writemore) {
										key.interestOps(key.interestOps()
												| SelectionKey.OP_WRITE);
									}
								}
							}
						} catch (CancelledKeyException e) {}
					}
				}
			}
		});
		thread.start();
	}

	protected void onError(Exception e) {
		if(listener!=null)
			listener.onError(e);
	}
	
	public void close(){
		open = false;
		selector.wakeup();
		timeoutWait.notifyAll();
	}

	public void requestWrite(final SelectableChannel channel) {
		synchronized(tasklist){
			tasklist.add(new Runnable() {
				public void run() {
				    SelectionKey key = channel.keyFor(selector);
					key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
				}
			});
		}
		selector.wakeup();
	}

	public void register(final SelectableChannel channel, final int interstop,
			final Object obj) {
		synchronized(tasklist){
			tasklist.add(new Runnable() {
				public void run() {
					if (!channel.isOpen() && obj instanceof ExceptionListener) {
						((ExceptionListener) obj).onError(new IOException(
								"Channel not open"));
					}
	
					try {
						if (channel.isRegistered()) {
							SelectionKey key = channel.keyFor(selector);
							key.interestOps(key.interestOps()
									| interstop);
							key.attach(obj);
						} else {
							channel.configureBlocking(false);
							channel.register(selector, interstop, obj);
						}
					} catch (Exception e) {
						if (obj instanceof ExceptionListener)
							((ExceptionListener) obj).onError(e);
					}
				}
			});
		}
		selector.wakeup();
	}

}
