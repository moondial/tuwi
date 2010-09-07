import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.MediaImage;


/**
 * Box Model 風実装
 */
public class Box {
	static int SMALLER=-4, LARGER=+4, BOLD=1, ITALIC=2, BOLDITALIC=3,
				UNDERLINE=0x100, OVERLINE=0x200, MIDDLELINE=0x400,
				右揃え=0x10000, 中央揃え=0x20000, NOBREAK=0x40000;
	static Graphics g;
	static int auto = Integer.MAX_VALUE;
	static int 最小幅 = 50;
	
	
	private Box parent;
	public int 左, 右, 上, 下, // margin
				幅, 高さ, // inner box size
				_x,	_y, // temp vers for rendering
				_h, // line-height
				x, y; // distance from display origin(0,0)
	private Font font = Font.getDefaultFont();
	
	public Box() {
	}
	
	public Box(int X, int Y, int w, int h) {
		_x = x = X;
		_y = y = Y;
		幅 = w;
		高さ = h;
	}
	
	public Box info(String id) {
		Tuwi.log(id+">> x: "+x+", y: "+y+", _x: "+_x+", _y: "+_y+", _h: "+_h+", boxW: "+boxWidth()+", boxH: "+boxHeight());
		return this;
	}
	
	public int boxWidth() {
		if(幅 == auto)
			return parent.x + parent.左 + parent.幅 - x;
		return 左 + 幅 + 右;
	}
	
	public int boxHeight() {
		if(高さ == auto)
			return _y + _h - y + 下;
		return 上 + 高さ + 下;
	}
	
	public Box child() {
		Box c = new Box(_x, _y, auto, auto);
		c.parent = this;
		return c;
	}
	
	/**
	 * auto 指定で自動計算
	 * @param left 左マージン
	 * @param width ボックス幅
	 * @param right 右マージン
	 * @return メソッドチェーン可
	 */
	public Box setWidth(int left, int width, int right) {
		左 = left;
		幅 = width;
		右 = right;
		if(width == auto) {
			if((parent.幅 - parent._x) < 左 + 右 + 最小幅)
				parent.newLine();
			幅 = (parent.幅 - parent._x) - 左 - 右;
		} else {
			if((parent.幅 - parent._x) < 左 + 右 + 幅)
				parent.newLine();
			if(左 != 右)
				if(左 == auto)
					左 = (parent.幅 - parent._x) - 幅 - 右;
				else
					右 = (parent.幅 - parent._x) - 幅 - 左;
		}
		_x = x + 左;
		return this;
	}
	
	// 高さ以外にはauto設定不可
	public Box setHeight(int top, int height, int bottom) {
		上 = top;
		高さ = height;
		下 = bottom;
		_y = y + 上;
		if(height == auto)
			height = _y - y;
		if(parent._h < 上+height+下)
			parent._h = 上+height+下;
		return this;
	}
	
	public Box slide(int _) {
		_y += _;
		return this;
	}
	
	// newLineと_hはstr(0, "");してから使うこと。
	public Box newLine() {
		// 下の行へ
		_y += _h;
		// 行の高さを戻す
		_h = font.getHeight();
		// 左端に戻る
		_x = x + 左;
		// method chain
		return this;
	}
	
	// 簡単文字列描画
	public Box str(int hex, String s) {
		return str(hex, s, 0, 0);
	}
	
	// 拡張文字列描画(文字色[0xrrggbb]、文字、背景色[0xaarrggbb]、装飾[consts]);
	//public Box str(int color, String str, int bg, int dec) {
	//	return str(color, str, bg, dec);
	//}
	//TODO: child()内の拡大がparentの_hに影響するようにする refrect2p()
	//TODO: 行の高さの指定、nobreak
	// 拡張文字列描画関数(文字色[0xrrggbb]、文字、背景色[0xaarrggbb]、装飾[consts]);
    public Box str(int color, String str, int bg, int dec) {
    	String sub;
		int _w = 0, startAt = 0, endAt = 0, len = str.length();
		// フォント設定
		dec += 4;
		font = MainCanvas.fonts[dec & 0xff];
		g.setFont(font);
		// 行の高さを設定
		if (font.getHeight() > _h)
			_h = font.getHeight();
		// 改行必要?
		if (幅 <= _x - 左 - x || (dec & NOBREAK) != 0 && x + 左 + 幅 - _x < font.getBBoxWidth(str))
			newLine();
		// キャンセルしないと崩れる
		if(len == 0)
			return this;
		// 間近の改行位置
		int next = str.indexOf('\n');
		rgb(color);
		while (len > 0) {
			// 初期化
			startAt = endAt;
			endAt = font.getLineBreak(str, startAt, len, x + 左 + 幅 - _x);
			
			// 改行が存在する
			if(startAt <= next && next < endAt) {
				endAt = ++next;
				next = str.indexOf('\n', endAt);
			}
			
			//Tuwi.log("index="+next+" "+endAt);
			sub = str.substring(startAt, endAt);
			//Tuwi.log("|"+sub+";");
			// _w は文字幅
			_w = font.getBBoxWidth(sub);
			if((dec & 右揃え) != 0)
				_x = x + 左 + 幅 - _w;
			// 背景描画
			if(bg != 0) {
				rgba(bg);
				//if(boxes != null) boxes.addElement(new Box(_x, _y, _w, font.getHeight()));
				g.fillRect(_x, _y, _w, font.getHeight());
				rgb(color);
			}
			// 文字描画
			g.drawString(sub, _x, _y + font.getAscent());
			// ここから _w は行はじめの位置から文字列末端までの幅
			_w += _x;
			
			if(dec != 4) {
				// 下線
				if ((dec & UNDERLINE) != 0)
					g.drawLine(_x, _y + font.getAscent(), _w, _y + font.getAscent());
				// 上線
				if ((dec & OVERLINE) != 0)
					g.drawLine(_x, _y, _w, _y);
				// 取り消し線
				if ((dec & MIDDLELINE) != 0)
					g.drawLine(_x, _y + font.getAscent() / 2, _w, _y + font.getAscent() / 2);
			}
			// 下の行へ
			_y += _h;
			// 行の高さを戻す
			_h = font.getHeight();
			// 左端に戻る
			_x = x + 左;
			// 残り文字数
			len -= endAt - startAt;
			//Tuwi.log("str"+len);
		}
		// 文字の右端の座標
		_x = _w;
		// 一行だけ戻してあわせる
		_y -= font.getHeight();
		// メソッドチェーン
		return this;
	}
    
    // 描画しない版str()
    public Box measure(String str, int dec) {
		int _w = 0, startAt = 0, endAt = 0, len = str.length();
		dec += 4;
		font = MainCanvas.fonts[dec & 0xff];
		g.setFont(font);
		// 行の高さを設定
		if (font.getHeight() > _h)
			_h = font.getHeight();
		// 改行必要?
		if (幅 <= _x - 左 - x || (dec & NOBREAK) != 0 && x + 左 + 幅 - _x < font.getBBoxWidth(str))
			newLine();
		// キャンセルしないと崩れる
		if(len == 0)
			return this;
		// 間近の改行位置
		int next = str.indexOf('\n');
		while (len > 0) {
			// 初期化
			startAt = endAt;
			endAt = font.getLineBreak(str, startAt, len, 幅 - (_x - 左 - x));
			
			// 改行が存在する
			if(startAt <= next && next < endAt) {
				endAt = ++next;
				next = str.indexOf('\n', endAt);
			}
			// _w は文字幅+行はじめの位置から
			_w = font.getBBoxWidth(str.substring(startAt, endAt)) + _x;
			// 下の行へ
			_y += _h;
			// 行の高さを戻す
			_h = font.getHeight();
			// 左端に戻る
			_x = x + 左;
			// 残り文字数
			len -= endAt - startAt;
			//Tuwi.log("str"+len);
		}
		// 文字の右端の座標
		_x = _w;
		// 一行だけ戻してあわせる
		_y -= font.getHeight();
		return this;
    }
    
	// 拡張文字列描画関数(文字色[0xrrggbb]、ラベル、境界線色[0xaarrggbb]、装飾[consts]);
    public Box button(int color, String str, int bc, int dec) {
    	int _w = 0;
		font = MainCanvas.fonts[(dec + 4) & 0xff];
		g.setFont(font);
		
		// ボタンが行内におさまらない場合
		if(font.stringWidth(str) > 幅 - (_x - 左 - x) && _x != x + 左)
			newLine();
		// ボタンが次行におさまる場合
		if((_w = font.stringWidth(str)) <= 幅) {
    		// グラデに置き換え予定
			rgba(0x80fafaff);
    		g.fillRect(_x, _y, _w + font.getHeight(), font.getHeight());
    		rgba(bc);
    		g.drawRect(_x, _y, _w + font.getHeight(), font.getHeight());
    		_x += font.getHeight() / 2;
    		rgb(color);
    		str(color, str, 0, dec);
    	} else {
    	// 複数行ボタン描画
    		Box b = child().setWidth(font.getHeight()/2, (int)(幅 * 0.75), font.getHeight()/2).setHeight(0, auto, 0)
    		.measure(str, dec).rgba(0x80fafaff).fill().rgba(bc).rect();
    		_h = b.boxHeight();
    		_x = b.x + b.boxWidth();
    		b._x = b.x + b.左;
    		b._y = b.y + b.上;
    		b.str(color, str, 0, dec);
    	}
		_x += font.getHeight() / 2;
    	return this;
    }
    
	// 配色変更便利メゾッド
	public Box rgb(int hex) {
		g.setColor(Graphics.getColorOfRGB(hex >> 16 & 0xff, hex >> 8 & 0xff, hex & 0xff));
		return this;
	}
	
	// 配色変更便利メゾッド
	public Box rgba(int hex) {
		g.setColor(Graphics.getColorOfRGB(hex >> 16 & 0xff, hex >> 8 & 0xff, hex & 0xff, hex >> 24 & 0xff));
		return this;
	}
	
	public Box rgba2(boolean b, int link, int focus) {
		if(b) rgba(focus); else rgba(link);
		return this;
	}
	
	public Box rect() {
		g.drawRect(x, y, boxWidth(), boxHeight());
		return this;
	}
	
	public Box fill() {
		g.fillRect(x, y, boxWidth(), boxHeight());
		return this;
	}
	
	public Box clip() {
		g.setClip(x, y, boxWidth(), boxHeight());
		return this;
	}
	
	public Box parent() {
		g.clearClip();
		return this.parent;
	}
	
	public void clear() {
		g.clearRect(x, y, boxWidth(), boxHeight());
	}
	
	/*public boolean hit(int X, int Y) {
		if(y <= Y && Y <= y+上+高さ+下 && x <= X && X <= x+左+幅+右)
			return true;
		return false;
	}*/
	
	//public int[] capture(int[] arg) {
	//	return g.getRGBPixels();
	//}
	
	//public Box restore(int[] arg) {
	//	g.setRGBPixels();
	//	return this;
	//}
	
	public Box img(MediaImage m) {
		if(m != null)
			g.drawImage(m.getImage(), _x, _y);
		return this;
	}
	
	public Box img(MediaImage m, int w, int h) {
		if(m != null)
			g.drawScaledImage(m.getImage(), _x, _y, w, h, 0, 0, m.getWidth(), m.getHeight());
		_x += w;
		if(_h < h) _h = h;
		return this;
	}
	
	public Box chip() {
		return this;
	}
	
	// text_chunkのstart_at番目から描画。focus_at番目のとき選択色で描画。chunk_yposに画面のy座標を保存
	public Box bodystr(String[] text_chunk, int[] chunk_ypos, int start_at, int focus_at) {
		for (int j = start_at; j < text_chunk.length; ++j) {
			char c = text_chunk[j].charAt(0);
			if (c == ' ') {
				str(0, text_chunk[j].substring(1));
				continue;
			} else if (c == '@') {
				if (focus_at == j)
					str(0xFF6347, text_chunk[j], 0x7fFEEEF1, 1);
				else
					str(0xFF6347, text_chunk[j], 0, 0);
			} else if (c == '#') {
				if (focus_at == j)
					str(0x006400, text_chunk[j], 0x7ff0fff0, 1);
				else
					str(0x006400, text_chunk[j], 0, 0);
			} else { // 'h'
				if (focus_at == j)
					str(0x8888ff, text_chunk[j], 0x7ff5fffa, 1);
				else
					str(0x6666ff, text_chunk[j], 0, 0);
			}
			
			chunk_ypos[j] = _y;
		}
		return this;
	}
	// 本当はイベント対応とかもやりたいのだが。。。
	public Box linkstr(String s, int color, int focus, int bgcolor, boolean hasfocus) {
		if(hasfocus)
			str(focus, s, bgcolor, 1);
		else
			str(color, s, 0, 0);
		return this;
	}
	// TODO: v/h grads
}
