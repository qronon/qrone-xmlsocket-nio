package org.qrone.xmlsocket.inner;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.qrone.xmlsocket.nio.ProtocolDecoder;

public class XMLSocketProtocolDecoder implements ProtocolDecoder{
	public static final byte EOF = 0;
	private ByteArrayOutputStream packetbuf = new ByteArrayOutputStream();
	
	public byte[] decode(ByteBuffer inBuffer){
		byte[] packet = null;
		while (inBuffer.hasRemaining()) {
			byte b = inBuffer.get();
			if (b == EOF) {
				packet = packetbuf.toByteArray();
				packetbuf = new ByteArrayOutputStream();
				if(packet.length>0){
					return packet;
				}
			} else {
				packetbuf.write(b);
			}
		}
		return null;
	}
}
