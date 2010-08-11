package org.qrone.xmlsocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.qrone.XMLTools;
import org.qrone.xmlsocket.event.XMLSocketListener;
import org.qrone.xmlsocket.inner.XMLSocketProtocolDecoder;
import org.qrone.xmlsocket.inner.XMLSocketProtocolEncoder;
import org.qrone.xmlsocket.nio.ExceptionListener;
import org.qrone.xmlsocket.nio.SelectorSocket;
import org.qrone.xmlsocket.nio.SelectorThread;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * <br>
 * シングルスレッド通信は極めて多い通信セッションでも問題なく動作し、マルチスレッドよりも極めて高
 * い処理速度を実現できます。スレッドを　２～５０程度生成し振り分けることも考えられますが通信に関
 * しては　１スレッドで、２０００～５０００近い通信セッションを問題なく処理できます<br>
 * <br>
 * しかしスレッドから呼ばれる onData や onXML といった関数で長い時間のかかる処理、例えば
 * ファイル入出力等のブロックを伴う処理を行うと、その処理中全通信セッションがストップしてしまいます。
 * また巨大な計算処理なども問題となり得ます。<br>
 * 
 * @author Administrator
 */
public class XMLSocket extends SelectorSocket{
	private static final Logger log = Logger.getLogger(XMLSocket.class);

	/**
	 * XMLSocket　通信オブジェクトを生成します。ここから新しくオブジェクトをつくって新しい通信を行う
	 * ことができます。しかしこのコンストラクタでは新しく XMLSocketThread を生成する為。非常に
	 * 多くのオブジェクト（１０００～）を生成した場合にはスレッド数が大幅に増加し、処理能力の著しい
	 * 低下を招く可能性があります。
	 * 
	 * @see #XMLSocket(SelectorThread)
	 */
	public XMLSocket() throws IOException {
		this(new XMLSocketThread(new ExceptionListener(){
				public void onError(Exception e) {
					onError(e);
				}
			}));
	}

	/**
	 * XMLSocket 通信オブジェクトを生成します。 java.nio パッケージを利用しスレッド数を押さえた
	 * 通信を実現するために引数にスレッドオブジェクトを取っています<br>
	 * <br>
	 * スレッドは１００～２００程度であれば問題なく動作しますが、１０００～　のスレッド数は多くのＯＳで大
	 * きな処理能力の低下を引き起こします。その為、非常に多くの通信セッションを開く場合には、共通の
	 * スレッドを利用してスレッド数を押さえてください。<br>
	 * 
	 * @param thread 利用するスレッドを指定
	 */
	public XMLSocket(XMLSocketThread thread) {
		super(thread, 
				new XMLSocketProtocolEncoder(), 
				new XMLSocketProtocolDecoder());
	}
	
	XMLSocket(SelectorThread thread) {
		super(thread, 
				new XMLSocketProtocolEncoder(), 
				new XMLSocketProtocolDecoder());
	}
	
	private boolean parsexml = true;
	private LinkedList xmllistener = new LinkedList();
	
	private Charset inputcs  = Charset.forName("UTF-8");
	private Charset outputcs = Charset.forName("UTF-8");
	private String ipaddress = "";
	
	/**
	 * XMLSocket 通信に利用する文字コードのエンコーディングを指定します。
	 * 指定しない場合標準では UTF-8 が設定されていますが、通常の Flash と日本語で通信するには
	 * ShiftJIS である必要があります。
	 * 
	 * @param charset 入出力の文字コードを指定
	 */
	public void setEncoding(Charset charset){
		setEncoding(charset,charset);
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
		setEncoding(Charset.forName(input), Charset.forName(output));
	}
	
	/**
	 * 文字列 str を相手側に送ります。 XMLSocket 通信では str は通常 well-formed XML
	 * である 必要があります。
	 * 
	 * @param str 送信する文字列 （XML であるべきです）
	 */
	public void send(String str) {
		try {
			send(str.getBytes(outputcs.displayName()));
			log.debug("SEND " + ipaddress + " " + str);
		} catch (UnsupportedEncodingException e) {}
	}

	/**
	 * XML ドキュメントを相手側に送ります。
	 * 
	 * @param doc 送信する XML ドキュメント
	 */
	public void send(Document doc) throws TransformerException {
		Transformer t = XMLTools.transformerFactory.newTransformer();
		t.setOutputProperty(OutputKeys.ENCODING,outputcs.displayName());
		String str = XMLTools.write(doc,t);
		try {
			send(str.getBytes(outputcs.displayName()));
			log.debug("SEND " + ipaddress + " " + str);
		} catch (UnsupportedEncodingException e) {}
	}

	/**
	 * XML 解析を行うかどうかの設定をします。true にした場合には常に XML 解析が行われますが、
	 * false にすると XML　解析が行われなくなり、onXML(Document) が呼び出されることがなくなります。
	 * 
	 * @param bool
	 *            XML 解析の行う/行わない
	 */
	public void setXMLParsing(boolean bool) {
		parsexml = bool;
	}

	/**
	 * 接続を行っている Socket クラスのインスタンスを返します。
	 * 
	 * @return 接続中ソケット
	 */
	public Socket getSocket() {
		return getSocketChannel().socket();
	}

	/**
	 * 接続開始時に呼ばれ、XMLSocketListener に通知します。success == false の時は
	 * <b>通信が確立されていません。</b>
	 * その場合には通常 onError も呼ばれます。<BR>
	 * <BR>
	 * このクラスを継承したクラスを作る場合にはこのメソッドを継承することで onConnect(boolean) イベントを 
	 * 取得できます。
	 * 
	 * @param success
	 *            接続の正否
	 */
	public void onConnect(boolean success) {
		ipaddress = getSocket().getInetAddress().getHostAddress();
		for (Iterator iter = xmllistener.iterator(); iter.hasNext();) {
			((XMLSocketListener) iter.next()).onConnect(success);
		}
		if(success) log.debug("CONNECT " + ipaddress);
	}

	/**
	 * 切断完了時に呼ばれ、XMLSocketListener に通知します。<BR>
	 * <BR>
	 * このクラスを継承したクラスを作る場合にはこのメソッドを継承することで onClose() イベントを 取得できます
	 */
	public void onClose() {
		for (Iterator iter = xmllistener.iterator(); iter.hasNext();) {
			((XMLSocketListener) iter.next()).onClose();
		}
		log.debug("CLOSE " + ipaddress);
	}

	/**
	 * エラー時に呼ばれ、XMLSocketListener に通知します。<BR>
	 * <BR>
	 * このクラスを継承したクラスを作る場合にはこのメソッドを継承することで onError(Exception) イベントを 
	 * 取得できます。
	 * 
	 * @param e
	 *            エラー
	 */
	public void onError(Exception e) {
		for (Iterator iter = xmllistener.iterator(); iter.hasNext();) {
			((XMLSocketListener) iter.next()).onError(e);
		}
		log.debug("ERROR " + ipaddress,e);
	}

	/**
	 * 通信タイムアウト時に呼ばれ、XMLSocketListener に通知します。タイムアウトは６０秒ごとに 
	 * 発行されます。 <br>
	 * <br>
	 * <b>注意：</b><code>onTimeout()</code> は通信が行われていなければ例え接続が継続して
	 * いる場合でも発行されますが、長時間 <code>onTimeout()</code> が発行され続ける場合には
	 * 接続が切断されている可能性があります。クライアントを設計する場合には必ず定期的に PING （接続
	 * 相手の生存確認）を送り、この <code>onTimeout()</code> が発行されるタイミングで相手から
	 * の送信が長時間途絶している場合にはソケットを終了する処理が必要です。<BR>
	 * <BR>
	 * このクラスを継承したクラスを作る場合にはこのメソッドを継承することで <code>onTimeout()</code> イベントを 
	 * 取得できます
	 * 
	 * @see java.net.Socket#setSoTimeout(int)
	 */
	public void onTimeout() {
		for (Iterator iter = xmllistener.iterator(); iter.hasNext();) {
			((XMLSocketListener) iter.next()).onTimeout();
		}
		log.debug("TIMEOUT " + ipaddress);
	}
	
	public void onPacket(byte[] b) {
		try {
			onData(new String(b,inputcs.displayName()));
		} catch (UnsupportedEncodingException e) {}
	}
	
	/**
	 * データ受信時に呼ばれ、XMLSocketListener に通知し、XML 解析を行って onXML(Document) 
	 * を呼び出します。<BR>
	 * <BR>
	 * このクラスを継承したクラスを作る場合にはこのメソッドを継承することで onData(String) イベントを
	 * 取得できます。このメソッドを継承した場合、super.onData(String) を呼ばないと onXML(Document)
	 * イベントが呼ばれなくなります。
	 * 
	 * @see #onXML(Document)
	 */
	protected void onData(String data) {
		for (Iterator iter = xmllistener.iterator(); iter.hasNext();) {
			((XMLSocketListener) iter.next()).onData(data);
		}
		log.debug("DATA " + ipaddress + " " + data);
		try {
			onXML(XMLTools.read(data));
		} catch (SAXException e) {}
	}

	/**
	 * データ受信後のさらに XML 解析後、に呼ばれ、XMLSocketListener に通知します。<BR>
	 * <BR>
	 * このクラスを継承したクラスを作る場合にはこのメソッドを継承することで onXML(Document) イベントを
	 * 取得できます。このイベントを取得するには setXMLParseing(boolean) に true (default)
	 * が設定されている必要があります。
	 */
	protected void onXML(Document doc) {
		for (Iterator iter = xmllistener.iterator(); iter.hasNext();) {
			((XMLSocketListener) iter.next()).onXML(doc);
		}
	}

	/**
	 * イベントハンドラを登録します。このメソッドを利用して XMLSocketListener を実装したクラスを
	 * 登録してイベントを取得、適宜処理を行ってください。
	 * 
	 * @param listener
	 *            イベントハンドラ
	 */
	public void addXMLSocketListener(XMLSocketListener listener) {
		xmllistener.add(listener);
	}

	/**
	 * 登録したイベントハンドラを削除します。
	 * 
	 * @param listener
	 *            イベントハンドラ
	 */
	public void removeXMLSocketListener(XMLSocketListener listener) {
		xmllistener.remove(listener);
	}
}
