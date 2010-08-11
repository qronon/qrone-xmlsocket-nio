package org.qrone.xmlsocket.nio;

import java.nio.ByteBuffer;

public interface ProtocolDecoder {
	public byte[] decode(ByteBuffer bBuffer);
}