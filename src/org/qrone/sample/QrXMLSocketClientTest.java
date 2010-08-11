package org.qrone.sample;

import java.io.IOException;
import java.net.UnknownHostException;

import org.qrone.xmlsocket.XMLSocket;
import org.qrone.xmlsocket.XMLSocketThread;
import org.qrone.xmlsocket.event.XMLSocketListener;
import org.w3c.dom.Document;

/**
 * 送られてきた XML を接続中の全員にそのまま送るサーバー（port:9601）のサンプル。
 * 
 * @author J.Tabuchi
 * @since 2005/8/6
 * @version 1.0
 * @link QrONE Technology : http://www.qrone.org/
 */
public class QrXMLSocketClientTest {
	// サーバーの待ちうけポート番号
	public static final int SERVER_PORT = 9601;
	
	public static void main(String[] args){
		int count=1;
		XMLSocketThread st = null;
		try {
			st = new XMLSocketThread();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		for(;count<2;count++){
			final int clientnumber = count;
			final XMLSocket socket = new XMLSocket(st);
			// クライアントのイベントハンドラの登録
			socket.addXMLSocketListener(new XMLSocketListener(){
				//　接続開始時
				public void onConnect(boolean success) {
					System.out.println("flash:"+clientnumber+":connect:");
	
					socket.send("<?xml version=\"1.0\" encoding=\"Shift_JIS\"?>"+
								"<Message date=\"テスト\"/>");
				}
				
				// 接続終了時
				public void onClose() {
					System.out.println("flash:"+clientnumber+":close:");
				}
	
				// エラー
				public void onError(Exception e) {
					e.printStackTrace();
				}
	
				//　タイムアウト
				public void onTimeout() {
					System.out.println("flash:"+clientnumber+":timeout");
				}
	
				// Flash からのデータ受信
				public void onData(String data) {
					System.out.println("flash:"+clientnumber+":data:"+data);
				}
	
				// Flash から受信したデータの XML DOM
				public void onXML(Document doc) {
				}
			});
			
			// サーバーを開始する
			try {
				socket.connect("localhost",9601);
				Thread.sleep(10);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
