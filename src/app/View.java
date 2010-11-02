package app;
import models.Account;

import org.apache.regexp.RE;


public abstract class View {
	public static final int[] emptyEvent = {
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,// 0x0n = 0 1 2 3 4 5 6 7 8 9 * # ----
		0,0,0,0,0,0,0,0,0/*未使用*/// 0x1n(basic) = ←↑→↓決①②- 消- - - ----
		};
	
	public static int[] cpyarr(int[] o) {
		int[] n = new int[o.length];
		for(int i=0; i<o.length; ++i)
			n[i] = o[i];
		return n;
	}
	
	/*public static Object[] join(Object[] o, Object[] oo) {
	    Object[] ooo = new Object[o.length + oo.length];
	    System.arraycopy(o, 0, ooo, 0, o.length);
	    System.arraycopy(oo, 0, ooo, o.length, oo.length);
	    return ooo;
	}*/
	
	// urlizeについて - 分割ルール
	// ・http://, https://ではじまるならリンク
	// ・半角@で始まるならならユーザ名
	// ・半角#で始まるならハッシュタグ
	// ・ハッシュタグ(15字まで)とリンクは全角を許容
	public static RE urlize = new RE("(@[\\w_]*|#[\\-\\w\\d_〃-\\u9fa5\\uf900-\\ufa6a]{1,15}|https?://[\\w;/?:@&=+$-_.!~*()#〃-\\u9fa5\\uf900-\\ufa6a]+)");
    public static boolean calculating;
	static int lineHei, lineHeiS, lineHeiL; // line height (midium, small, large)
	String title = "";
	int titlecolor, titledeco;
	MainCanvas mc;
	int //y, height, fAscent,//けすよてい
	_h, _x, _y, xx = 0/*改行時にもどる位置*/;
	//Font font;
	Account account;
	
	public int[] onKeyPress = cpyarr(emptyEvent),
               onKeyRepeat = cpyarr(emptyEvent),
               onKeyRelease = cpyarr(emptyEvent);
	
	View() {
		mc = MainCanvas.self;
		//font(0, (int)Tuwi.conf.Long("文字サイズ"));
	}
	
	public abstract void render(Box b);
	
	/**
	 * 以下予約済みイベント番号(ev)。
	 *  0 空イベント
	 *  1 View選択時
	 *  2 View切替時
	 *  3 タイマーイベント
	 *  4-15 とりあえず予約（使途未定）
	 *  
	 * trueを返した場合後続のViewにもキーイベントを渡す
	 * Popupなどでfalseを返せばTabViewにキーイベントを渡さない
	 */
	public abstract boolean handleEvent(int ev, Object o);
	public boolean handleEvent(int ev) {
		return handleEvent(ev, null);
	}
	/*
	class Event {
		Object target;
		int ev;
		Vector boxes;
		Event(Object o, int i, Vector b) {
			target = o;
			ev = i;
			boxes = b;
		}
		void call() {
			handleEvent(ev, target);
		}
		void calliftouched(int x, int y) {
			for(int i=0; i<boxes.size(); ++i)
				if(((Box)boxes.elementAt(i)).hit(x, y))
					call();
		}
	}
	
	Vector events = new Vector();
	
	public void drawlink(Box b, int ev, Object target, String linkstr, boolean selected) {
		Vector v = new Vector();
		if (selected)
			b.str(0x8888ff, linkstr, 0x7ff5fffa, Box.UNDERLINE);
		else
			b.str(0x6666ff, linkstr, 0, 0);
		//if()
			events.addElement(new Event(target, ev, v));
	}
	* /
	
	/**
	 * @param len 全体要素数（ページの長さ）
	 * @param begin 最初の要素番号（画面上y位置）
	 * @param end 最後の要素番号（画面下y位置）
	 */
	public void drawScrollBar(int len, int begin, int end) {
		if(begin > end) {
			Tuwi.log("scroll bar: "+begin+","+end);
			return;
		}
		new Box(mc.getWidth() - 8,
				mc.getHeight() * begin / len,
				8,
				mc.getHeight() * (end - begin) / len + 1)
		.rgba(0xaaccccff).fill();
	}
	
}
