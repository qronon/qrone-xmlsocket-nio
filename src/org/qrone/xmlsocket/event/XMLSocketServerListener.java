package org.qrone.xmlsocket.event;

import org.qrone.xmlsocket.XMLSocket;

/**
 * XMLServerSocket 用イベントハンドラインターフェース<BR>
 * <BR>
 * 適宜このインターフェースを実装したクラスを作り、XMLServerSocket.addXMLServerSocketListener(XMLSocketListener)
 * で登録しイベントを取得してください。
 * 
 * @author J.Tabuchi
 * @since 2005/8/6
 * @version 1.0
 * @link QrONE Technology : http://www.qrone.org/
 */
public interface XMLSocketServerListener {
	/**
	 * サーバー開始時に呼ばれます。 success == false の時は<b>開始に失敗しています。</b>十分注意してください。
	 * @param success サーバー開始成否
	 */
	public void onOpen(boolean success);
	/**
	 * サーバー終了時に呼ばれます。通常終了時のみならず、エラー終了時にも必ず呼ばれます。
	 */
	public void onClose();
	/**
	 * エラー時にのみ呼ばれます。理由となった Exception を返します。
	 * @param e エラー
	 */
	public void onError(Exception e);
	/**
	 * 新たに Macromedia Flash の .swf ファイルから XMLSocket 通信を要求された時に
	 * 呼び出され、swf ファイルと通信を確立する直前の XMLSocket オブジェクトが渡されます。<BR>
	 * @param xmlsocket　接続を確立した XMLSocket オブジェクト
	 */
	public void onNewClient(XMLSocket xmlsocket);
}
