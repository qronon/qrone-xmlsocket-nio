package org.qrone.xmlsocket.inner;

import java.nio.ByteBuffer;

import org.qrone.xmlsocket.nio.ProtocolEncoder;

public class XMLSocketProtocolEncoder implements ProtocolEncoder{
	public static final byte EOF = 0;
	public ByteBuffer encode(byte[] b) {
		byte[] n = new byte[b.length+1];
		System.arraycopy(b,0,n,0,b.length);
		n[n.length-1] = EOF;
		return ByteBuffer.wrap(n);
	}
}
