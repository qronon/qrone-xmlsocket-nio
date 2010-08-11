package org.qrone.xmlsocket;

import java.io.IOException;

import org.qrone.xmlsocket.nio.ExceptionListener;
import org.qrone.xmlsocket.nio.SelectorThread;

public class XMLSocketThread extends SelectorThread{
	public XMLSocketThread() throws IOException {
		super();
	}
	
	public XMLSocketThread(ExceptionListener listener) throws IOException {
		super(listener);
	}
}
