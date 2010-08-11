package org.qrone.xmlsocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.qrone.xmlsocket.event.XMLSocketServerListener;
import org.qrone.xmlsocket.nio.SelectorServerSocket;

public class XMLSocketServer extends SelectorServerSocket{
	private static final Logger log = Logger.getLogger(XMLSocketServer.class);
	
	public XMLSocketServer() throws IOException {
		super(new XMLSocketThread());
	}
	public XMLSocketServer(XMLSocketThread thread) {
		super(thread);
	}

	private static final int SERVER_TIMEOUT = 30000;
	private LinkedList serverlistener = new LinkedList();

	private Charset inputcs  = Charset.forName("UTF-8");
	private Charset outputcs = Charset.forName("UTF-8");

	/**
	 * XMLSocket 通信に利用する文字コードのエンコーディングを指定します。
	 * 指定しない場合標準では UTF-8 が設定されていますが、通常の Flash と日本語で通信するには
	 * ShiftJIS である必要があります。
	 * 
	 * @param charset 入出力の文字コードを指定
	 */
	public void setEncoding(Charset cs){
		setEncoding(cs,cs);
	}

	/**
	 * XMLSocket 通信に利用する文字コードのエンコーディングを指定します。
	 * 
	 * @see #setEncoding(Charset);
	 * @param charset 入出力の文字コードを指定
	 */
	public void setEncoding(String charset){
		setEncoding(Charset.forName(charset));
	}

	/**
	 * XMLSocket 通信に利用する文字コードのエンコーディングを指定します。
	 * 
	 * @see #setEncoding(Charset);
	 * @param input 入力の文字コードを指定
	 * @param output 出力の文字コードを指定
	 */
	public void setEncoding(Charset input, Charset output){
		inputcs = input;
		outputcs = output;
	}

	/**
	 * XMLSocket 通信に利用する文字コードのエンコーディングを指定します。
	 * 
	 * @see #setEncoding(Charset);
	 * @param input 入力の文字コードを指定
	 * @param output 出力の文字コードを指定
	 */
	public void setEncoding(String input, String output){
		setEncoding(Charset.forName(input),Charset.forName(output));
	}

	/**
	 * XMLSocket 通信に利用する文字コードの入力エンコーディングを取得します。
	 */
	public Charset getInputEncoding(){
		return inputcs;
	}
	/**
	 * XMLSocket 通信に利用する文字コードの出力エンコーディングを取得します。
	 */
	public Charset getOutputEncoding(){
		return outputcs;
	}
	
	/**
	 * XMLSocketServer　サーバーに関連づけられている ServerSocket インスタンスを返します。
	 * @return 利用中のサーバーソケット
	 */
	public ServerSocket getServerSocket(){
		return serverchannel.socket();
	}
	
	/**
	 * サーバー開始直後に呼び出されます。 success == false の時には
	 * <b>サーバーが開始されていません。</b><BR>
	 * 継承したクラスでこのメソッドをオーバーライドするとイベントハンドラのイベントが呼ばれなくなります。<BR>
	 * <BR>
	 * 通常は addXMLSocketServerListener(XMLSocketServerListener) 
	 * でイベントハンドラを利用してください。
	 * @see #addXMLSocketServerListener(XMLSocketServerListener)
	 * @param success サーバー開始正否
	 */
	public void onOpen(boolean success){
 		for (Iterator iter = serverlistener.iterator(); iter.hasNext();) {
			((XMLSocketServerListener)iter.next()).onOpen(success);
		}
 		if(success) log.debug("OPEN");
	}

	/**
	 * エラー直後に呼び出されます。<BR>
	 * 継承したクラスでこのメソッドをオーバーライドするとイベントハンドラのイベントが呼ばれなくなります。<BR>
	 * <BR>
	 * 通常は addXMLSocketServerListener(XMLSocketServerListener) 
	 * でイベントハンドラを利用してください。
	 * @see #addXMLSocketServerListener(XMLSocketServerListener)
	 * @param e エラー
	 */
	public void onError(Exception e) {
 		for (Iterator iter = serverlistener.iterator(); iter.hasNext();) {
			((XMLSocketServerListener)iter.next()).onError(e);
		}
 		log.debug("ERROR");
	}

	/**
	 * サーバー終了直後に呼び出されます。<BR>
	 * 継承したクラスでこのメソッドをオーバーライドするとイベントハンドラのイベントが呼ばれなくなります。<BR>
	 * <BR>
	 * 通常は addXMLSocketServerListener(XMLSocketServerListener) 
	 * でイベントハンドラを利用してください。
	 * @see #addXMLSocketServerListener(XMLSocketServerListener)
	 */
	public void onClose(){
 		for (Iterator iter = serverlistener.iterator(); iter.hasNext();) {
			((XMLSocketServerListener)iter.next()).onClose();
		}
 		log.debug("CLOSE");
	}

	/**
	 * 新たに Macromedia Flash の .swf ファイルから XMLSocket 通信を要求された時に
	 * 呼び出され、swf ファイルと通信を確立する直前の XMLSocket オブジェクトが渡されます。<BR>
	 * 継承したクラスでこのメソッドをオーバーライドするとイベントハンドラのイベントが呼ばれなくなります。<BR>
	 * <BR>
	 * 通常は addXMLSocketServerListener(XMLSocketServerListener) 
	 * でイベントハンドラを利用してください。
	 * @see #addXMLSocketServerListener(XMLSocketServerListener)
	 */
	public void onNewClient(XMLSocket socket){
 		for (Iterator iter = serverlistener.iterator(); iter.hasNext();) {
			((XMLSocketServerListener)iter.next()).onNewClient(socket);
		}
 		log.debug("NEWCLIENT " 
 				+ socket.getSocket().getInetAddress().getHostAddress());
	}
	
	/**
	 * イベントハンドラを登録して各種イベントを取得します。
	 * @param listener　イベントハンドラ
	 */
	public void addXMLSocketServerListener(XMLSocketServerListener listener) {
		serverlistener.add(listener);
	}

	/**
	 * 登録されているイベントハンドラを削除します。
	 * @param listener　イベントハンドラ
	 */
	public void removeXMLSocketServerListener(XMLSocketServerListener listener) {
		serverlistener.remove(listener);
	}
}
