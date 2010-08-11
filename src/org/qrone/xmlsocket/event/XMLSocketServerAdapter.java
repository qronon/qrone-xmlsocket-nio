package org.qrone.xmlsocket.event;

import org.qrone.xmlsocket.XMLSocket;

/**
 * 何もしない、ただ全ての関数を定義しただけの XMLServerSocketListener へのアダプタクラス
 * @author J.Tabuchi
 * @since 2005/8/6
 * @version 1.0
 * @link QrONE Technology : http://www.qrone.org/
 */
public class XMLSocketServerAdapter {
	public void onOpen(boolean success){}
	public void onError(Exception e){}
	public void onClose(){}
	public void onNewClient(XMLSocket socket){}
}
