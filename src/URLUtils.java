import java.io.*;

import javax.microedition.io.Connector;

import msgpack.*;

import org.apache.regexp.RE;

import com.nttdocomo.io.ConnectionException;
import com.nttdocomo.io.HttpConnection;
import com.nttdocomo.ui.Dialog;
import com.nttdocomo.util.JarFormatException;
import com.nttdocomo.util.JarInflater;

/**
 * 接続関連とスレッド
 */
public class URLUtils extends Thread{
	public static byte dlarray[];
	public static int loadedBytes = 0;
	public static long lastReqAt = 0; // 最終接続時 0: iモード未接続
	public static HttpConnection con = null;
	
	// HTTPステータスコード
	public int code;
	// レスポンス本体
	public Object msg;
	// メタデータ（つまり何か）
	public ExHash meta;
	
	private URLUtils() {}

	public static final int ZIPPED = 1, PLAIN_TEXT = 2, PACKED = 4;
	
	/**
	 * 引数を簡易エンコードします。値がnullだと自動省略するおまけ付き。
	 * @param s {key1, value1, key2, value2, ...}
	 * @return key1=value1&key2=value2& ...
	 * */
	public static String urlencode(String[] s) {
		StringBuffer url = new StringBuffer();
		for(int i=0; i < s.length/2; ++i) {
			// if value is null, skip it. 
			if(s[i*2 + 1] == null)
				continue;
			if(i > 0)
				url.append('&');
			url.append(s[i*2]);
			url.append('=');
			url.append(com.nttdocomo.net.URLEncoder.encode(s[i*2 + 1]));
			/* encode for tuwi
			for(int ii=0; ii<s[i*2 + 1].length(); ++ii)
				switch(s[i*2 + 1].charAt(ii)) {
				case '+':
					url.append("%2B");
					break;
				case '&':
					url.append("%26");
					break;
				case ';':
					url.append("%3B");
					break;
				default:
					url.append(s[i*2 + 1].charAt(ii));
				}
			*/
		}
		return url.toString();
	}
	// Request(接続先アドレス "tuwi/[user_token]/u",
	//          POST内容(あればPOSTぬるぽならGET) "status=Hello",
	//          レスポンスの中身 -> 圧縮済: +1, 平文: +2, MessagePack: +4)
	public static synchronized URLUtils Request(String path, String data, int type) {
		InputStream in = null;
		OutputStream o = null;
		ByteArrayOutputStream outstr = new ByteArrayOutputStream();
		URLUtils res = new URLUtils();
		int code = -1;
		
		Tuwi.log("取得[" + Tuwi.BASE + path + " " + data + " " + type + "]");
		lastReqAt = System.currentTimeMillis();
		loadedBytes = 0;
		
		try {
			if(data != null) {
				con = (HttpConnection) Connector.open(Tuwi.BASE + path, Connector.READ_WRITE, true);
				con.setRequestMethod(HttpConnection.POST);
				o = con.openOutputStream();
				o.write(data.getBytes());
				o.close();
			} else {
				con = (HttpConnection) Connector.open(Tuwi.BASE + path, Connector.READ_WRITE, true);
				con.setRequestMethod(HttpConnection.GET);
			}
			try {
				con.connect();
			} catch(ConnectionException e) {
				if(!(e.getStatus() == ConnectionException.HTTP_ERROR ||
						e.getStatus() == ConnectionException.SYSTEM_ABORT)) {  // 401用機種対策
					String msg = e.getStatus() + " 接続エラーです。orz";
					switch(e.getStatus()) {
					case ConnectionException.UNDEFINED:
						msg = "(おそらく)サーバー休止中です。またあとでお試しください。";
						break;
					case ConnectionException.USER_ABORT:
						msg = "接続は中止されました＞_＜";
						break;
					case ConnectionException.TIMEOUT:
						msg = "一定時間返答がありませんでした。orz";
						break;
					case ConnectionException.OUT_OF_SERVICE:
						msg = "圏外またはパケット規制中です。orz";
						break;
					case ConnectionException.BUSY_RESOURCE:
						msg = "すでに通信中です。。。";
						break;
					}
					// 1000番台をエラーとする
					res.code = 1000 + e.getStatus();
					res.msg = msg;
					return res;
				}
			}
			try {
				code = con.getResponseCode();
			} catch(RuntimeException e) {
				res.code = 401;
				res.msg = "認証失敗＞＜";
				return res;
			}
			res.code = code;
			// 入力ストリームを取得
			in = con.openInputStream();
			int c;
			// 生バイナリ
			while ((c = in.read()) != -1)
				outstr.write((char) c);
			in.close();
			in = null;
			dlarray = outstr.toByteArray();
			outstr.close();
			
			// DL情報保存
			loadedBytes = dlarray.length;
			Tuwi.conf.incr("dlBytes", loadedBytes);
			Tuwi.conf.incr("dlCount", 1);
			Tuwi.conf.incr("dlPackets", (loadedBytes / 128) + 2);
			// このライブラリ内では保存しない
			//Tuwi.saveConf();
			
			//System.out.println(new String(dlarray));
			// 解凍
			/* from iMona.java
			if((type & GZIP) == GZIP){
				System.out.println("GZIP");
				int i;
				byte b[] = dlarray;
				byte b2[] = new byte[b.length-8 + 100];
				for(i = 0; i < b.length-8; i++){
					b2[i + 31] = b[i];
				}

				i = b.length-8;

				//pkzipのヘッダを捏造
				b2[0] = 'P';
				b2[1] = 'K';
				b2[2] = 0x03;
				b2[3] = 0x04;
				b2[4] = 0x14;
				b2[8] = 0x08;
				//b2[10] = (byte)0xA8;
				//b2[11] = (byte)0xA1;
				//b2[12] = 0x5E;
				//b2[13] = 0x3F;
				
				b2[14] = b[b.length-8];
				b2[15] = b[b.length-7];
				b2[16] = b[b.length-6];
				b2[17] = b[b.length-5];
				b2[18] = (byte)(i % 256);	i /= 256;
				b2[19] = (byte)(i % 256);	i /= 256;
				b2[20] = (byte)(i % 256);	i /= 256;
				b2[21] = (byte)i;
				b2[22] = b[b.length-4];
				b2[23] = b[b.length-3];
				b2[24] = b[b.length-2];
				b2[25] = b[b.length-1];
				b2[26] = 0x01;	//file name length
				b2[30] = 'a';	//file name
				b2[b.length + 23] = 'P';
				b2[b.length + 24] = 'K';
				b2[b.length + 25] = 0x01;
				b2[b.length + 26] = 0x02;
				b2[b.length + 27] = 0x14;
				for(i = 0; i < 26; i++){
					b2[b.length + 29 + i] = b2[i + 4];
				}
				i = b.length + 23;
				b2[b.length + 69] = 'a';
				b2[b.length + 70] = 'P';
				b2[b.length + 71] = 'K';
				b2[b.length + 72] = 0x05;
				b2[b.length + 73] = 0x06;
				b2[b.length + 78] = 0x01;
				b2[b.length + 80] = 0x01;
				b2[b.length + 82] = 0x2F;
				b2[b.length + 86] = (byte)(i % 256);	i /= 256;
				b2[b.length + 87] = (byte)(i % 256);	i /= 256;
				b2[b.length + 88] = (byte)(i % 256);	i /= 256;
				b2[b.length + 89] = (byte)i;
				dlarray = b2;
			}*/
			// ZIPかどうか判定
			if (dlarray.length > 0 && dlarray[0] == 'P'/*(type & ZIPPED) == ZIPPED*/) {
				//System.out.println("解凍");
				try {
				// zip解凍
					in = (new JarInflater(dlarray)).getInputStream("a");
				} catch (JarFormatException e) {
					in = null;
					// 容量的に圧縮がかからない場合もあるのでこの部分は削除
					//if(code == HttpConnection.HTTP_OK)
					//	dialog(Dialog.DIALOG_ERROR, "データエラー",
					//	"受信データの解凍に失敗しました。\nもう一度試してみてください。");
				}
			}
			// MessagePack
			if(con.getHeaderField("Content-Type") == null ||
					con.getHeaderField("Content-Type").equals("")) {
				//System.out.println("MessagePack");
				if (in == null)
					in = new ByteArrayInputStream(dlarray);
				res.msg = new Msgpack(in);
				((Msgpack)res.msg).close();
				
			}
			// 平文
			else if (con.getHeaderField("Content-Type").startsWith("text/html")) {
				//System.out.println("平文");
				if (in == null)  // 解凍してない
					res.msg = new String(dlarray);
				else {  // 解凍してる
					StringBuffer sb = new StringBuffer(dlarray.length);
					while ((c = in.read()) != -1)
						sb.append((char) c);
					res.msg = sb.toString();
				}
			} else {
				// 普通のバイナリ
				res.msg = dlarray;
			}
		} catch(IOException e) {
			Tuwi.dialog(Dialog.DIALOG_ERROR, "I/Oエラー", "原因不明っぽいです。（電池切れ？）");
			//e.printStackTrace();
		} finally {
			// リソースを開放
			if (in != null)  try { in.close();  } catch (Exception e) {}
			if (con != null) try { con.close(); } catch (Exception e) {}
			if (o != null)   try { o.close();   } catch (Exception e) {}
			Tuwi.log("HTTP " + res.code + " " + res.msg);
			Tuwi.log(System.currentTimeMillis() - lastReqAt + "ms " + loadedBytes + "bytes");
			System.gc();
		}
		return res;
	}
	
	// つゐにログインする。引数=true で強制ログイン。
	public static boolean Auth(Account a, boolean force) {
		if(!(a.token.equals("") || force)) return true;
		try {
			String t[] = new String[]{
					"u", a.userid(),
					"t", a.oauth_token,
					"s", a.oauth_token_secret,
					"p", a.password,
					"v", Tuwi.version};
			if(a.oauth_token.length() > 0)
				t[7] = null;
			URLUtils res = Request("tuwi/i", urlencode(t), PLAIN_TEXT);
			if(res.code == 200) {
				t = new RE("\\n").split(res.msg.toString());
				a.token = t[0];
				a.token_expire = Integer.parseInt(t[1]);
				Tuwi.log("新しいトークンは"+t[0]+"\nTuwi最新版は"+t[2]+"です。\n"+t[3]);
				return true;
			}
			else if(res.code == HttpConnection.HTTP_NOT_AUTHORITATIVE)
				Tuwi.dialog(Dialog.DIALOG_ERROR, "認証エラー",
					"認証に失敗しました。\nユーザー名とパスワードを確認してください。");
			else
				Tuwi.dialog(Dialog.DIALOG_INFO, "エラー", res.msg.toString());
		}catch (Exception e) {
			Tuwi.log(e.getMessage());
		}
		return false;
	}
	
	/*public static Msgpack APIRequest(Account a, String url, String d) {
		URLUtils res;
		String title, msg;
		Auth(a, false);
		if(a.token.equals("")) return null;
		res = Request("tuwi/" + a.token + "/" + url, d, ZIPPED | PACKED);
		if(res.code == 401) {
			Auth(a, true);
			res = Request("tuwi/" + a.token + "/" + url, d, ZIPPED | PACKED);
		}
		switch(res.code) {
		case 200:
			return (Msgpack)res.msg;
		case 400:
			title = "400 不正リクエスト";
			msg = "1時間の接続回数制限(API制限)に達したためリクエストが却下されました。少し待ってね。";
			break;
		case 403:
			title = "403 リクエスト不可";
			msg = "権限の無い情報を取得しようとしたため、却下されました。";
			break;
		case 404:
			title = "404 Not Found";
			msg = "もう存在しないユーザやつぶやきが指定されていました。";
			break;
		case 500:
			title = "500 サーバエラー";
			msg = "TuwiもしくはTwitter側で何らかの問題が発生しています。";
			if(dlarray != null) {
				Tuwi.log(new String(dlarray));
			}
			break;
		case 502:
			title = "502 接続エラー";
			msg = "Twitterが落ちている、あるいはメンテナンス中のようです。";
			break;
		case 503:
			title = "503 サービス応答不能";
			msg = "Twitterの負荷が高すぎて……もう……だめっ……///";
			break;
		case 504:
			title = "504 接続エラー";
			msg = "TuwiのサーバがTwitterに接続できませんでした。";
			break;
		default:
			title = "サーバエラー " + res.code;
			msg = res.msg.toString();
			if(dlarray != null) {
				Tuwi.log(new String(dlarray));
			}
		}
		
		// 通信エラーは表示しない
		if(res.code < 1000)
			Tuwi.dialog(Dialog.DIALOG_WARNING, title, msg);
		return null;
	}
	*/
	
	
	public static URLUtils APIRequest(Account a, String url, String d) {
		URLUtils res;
		
		Auth(a, false);
		if(a.token.equals(""))
			return null;
		res = Request("tuwi/" + a.token + "/" + url, d, 0);
		
		if(res.code == 401) {
			Auth(a, true);
			res = Request("tuwi/" + a.token + "/" + url, d, 0);
		}
		
		try {
			res.meta = (ExHash)((Msgpack)res.msg).getObject();
			res.msg = ((Msgpack)res.msg).getObject();
			a.setMeta(res.meta);
		} catch (Exception e) {
			Tuwi.log(e+"\nmeta data error.");
		}
		return res;
	}
	
	// Threads
	public static void createThread(View v, int e, Object o) {
		URLUtils u = new URLUtils();
		u.code = e;
		u.msg = new Object[]{v, o};
		u.start();
	}
	
	public void run() {
		Tuwi.log("開始: " + Thread.currentThread().getName());
		((View)((Object[])msg)[0]).handleEvent(code, ((Object[])msg)[1]);
	}
}
