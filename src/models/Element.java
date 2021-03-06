package models;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import app.Tuwi;


/**
 * 「表示する要素」の基底クラスっぽいアレ
 * タイムライン表示に使用される
 * つぶやきやＤＭなどのためにidと日付対応
 * heightは要素の高さ, text_chunkは文字列の集合, text_yposは集合の各文字列のy位置
 * フォントが変更された場合heightが変わるがどうしようかな・・・
 **/
public class Element extends RichText {
	public long id, created_at;
	
	public int height, y;
	//public String[] text_chunk;  // [screen_name, text(1), test(2), ...]
	//public int[] text_ypos;  // [text_ypos(1), test_ypos(2), ...]リンク縦位置。描画時に保存される
	
	public Object[] src;  // 元データ
	
	//public Element prev, next;
	// 前後のリンクの縦位置を返す？
	//public int prevFocus(int f) { return -1; }
	//public int nextFocus(int f) { return -1; }
	//public Object click() { return src[1]; }
	//public void render(View v, int focus_at, int style) {
		//v.str(0, src[0].toString()).skipLine();
	//}
	//public void setHighlight(int _) {}
	
	Element(Object[] _) {
		src = _;
	}
	
	public Element(int ev, int mode, String t) {
		id = ev;
		created_at = mode;
		text = t;
	}
	
	public String getDate() {
		// 絶対時間/相対時間　日本語/英語
		// [n分前][n時間半前][深夜n時m分][nn:mm] < 24h < [nn月mm日HH:MM][nn/mm NN:MM]
		long delta = (System.currentTimeMillis() / 1000)/*now*/ - created_at;
		if(delta > 24*60*60)
			return getAbsDate();
		
		// 0: 絶対時刻, 1: 絶対時刻(秒まで), 2: 相対時間, 3: 相対時間(24時間で秒つき絶対時刻表示), 3<: なし
		int b = (int)Tuwi.conf.Long("日付書式");
		if(b > 3) return "";
		int i;
		String f;
		if(b < 2) {
			Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			cal.setTime(new Date(created_at * 1000));
			i = cal.get(Calendar.HOUR_OF_DAY);
			f = (i<5? "深夜": i<10? "朝": i<12? "午前": i<19? "午後": "夜")
				+ cal.get(Calendar.HOUR);
			i = cal.get(Calendar.MINUTE);
			f += (i<10 ? "時0" : "時") + i + "分";
			if(b == 1) {
				i = cal.get(Calendar.SECOND);
				f += (i<10 ? "0" : "") + i + "秒";
			}
		} else {
			if(delta > 3600*6)
				f = delta / 3600 + "時間前";
			else if(delta > 3600)
				f = delta / 3600 + "時間" + (delta % 3600) / 60 + "分前";
			else if(delta > 60*10)
				f = delta / 60 + "分前";
			else
				f = delta + "秒前";
		}
		return f;
	}
	
	public String getAbsDate() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.setTime(new Date(created_at * 1000));
		// 0: 絶対時刻, 1: 絶対時刻(秒まで), 2: 相対時間, 3: 相対時間(24時間で秒つき絶対時刻表示)
		String f = (cal.get(Calendar.MONTH) + 1) + "/"
			 + cal.get(Calendar.DATE) + " "
			 + cal.get(Calendar.HOUR_OF_DAY)
			 + (cal.get(Calendar.MINUTE) < 10 ? ":0" : ":")
			 + cal.get(Calendar.MINUTE);
		if(Tuwi.conf.Long("日付書式") % 2 == 1)
			f += (cal.get(Calendar.SECOND) < 10 ? ":0" : ":")
				+ cal.get(Calendar.SECOND);
		return f;
	}
	
	// w番目のリンクのY位置を返す
	public int getLinkYpos(int w) {
		if(w == 0) return y;
		return (format[w - 1] >> 12) & 0x0ffff;
	}
	
	//w番目のリンクの種類を返す
	public int getLinkType(int w) {
		if(w == 0) return RichText.LINK;
		return format[w - 1] >> 28;
	}
}
