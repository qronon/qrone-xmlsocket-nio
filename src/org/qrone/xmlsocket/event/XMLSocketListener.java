package org.qrone.xmlsocket.event;

import org.w3c.dom.Document;

/**
 * XMLSocket 用イベントハンドラインターフェース<BR>
 * <BR>
 * 適宜このインターフェースを実装したクラスを作り、XMLSocket.addXMLSocketListener(XMLSocketListener)
 * で登録しイベントを取得してください。<BR>
 * <BR>
 * このインターフェースは Macromedia Flash の ActiveScript オブジェクト XMLSocket とできる限り
 * 同じに作ってあります。
 * 
 * @author J.Tabuchi
 * @since 2005/8/6
 * @version 1.0
 * @link QrONE Technology : http://www.qrone.org/
 */
public interface XMLSocketListener {
	/**
	 * 接続開始時に呼ばれます。 success == false の時は<b>接続に失敗しています。</b>十分注意してください。
	 * @param success　接続開始成否
	 */
	public void onConnect(boolean success);
	/**
	 * 接続終了時に呼ばれます。通常終了時のみならず、エラー終了時にも必ず呼ばれます。
	 */
	public void onClose();
	/**
	 * エラー時にのみ呼ばれます。理由となった Exception を返します。
	 * @param e エラー
	 */
	public void onError(Exception e);
	/**
	 * 通信のタイムアウト時に呼ばれます。一定時間以上通信が行われていない場合に呼ばれる可能性がありますが、
	 * 設定によってはまったく呼び出されません。
	 */
	public void onTimeout();
	/**
	 * データを受信した時、XML 解析が行われる前に呼び出されます。
	 * @param data
	 */
	public void onData(String data);
	/**
	 * データ受信の後、 XML 解析をした上で呼び出されます。 DOM を利用したい場合には解析後のこちらを利用す
	 * ると簡単でよいでしょう。
	 * @param doc
	 */
	public void onXML(Document doc);
}
