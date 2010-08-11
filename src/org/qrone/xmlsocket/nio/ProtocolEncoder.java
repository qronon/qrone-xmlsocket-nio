package org.qrone.xmlsocket.nio;

import java.nio.ByteBuffer;

public interface ProtocolEncoder {
	public ByteBuffer encode(byte[] b);
}
