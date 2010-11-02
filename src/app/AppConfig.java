package app;


import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;

/**
 *
 */
public class AppConfig extends View {
	// static()メソッドの生成を防ぐためここには書かない
	private static Input[] SETS;
	private static MediaImage menuimg;
	private static int index;
	private static int[] f;
	
	public AppConfig() {
		super();
		title = "アプリ設定";
		String[] s;
		try {
			//name=0/0+"";
			f = Font.getSupportedFontSizes();
			index = f.length;
			//if(!Tuwi.DEBUG)
				for(index=0; f[index]<=mc.getWidth()/8 &&index<f.length; ++index);
			s = new String[index];
			for(int i=0; i<index; ++i)
				s[i] = f[i] + "pt";
			index = 0;
		} catch (Exception e) {
			s = new String[] {"微", "小", "中", "大"};
		}
		SETS = new Input[] {
				new Input(title),
				new Input("fontSize", "文字の大きさ", s),
				new Input("showIcons", "アイコン表示", true),
				new Input("autoIconDL", "アイコン自動取得", true),
				new Input("日付書式", "日付書式", new String[] {"常に絶対時刻", "常に絶対時刻(+秒)", "経過時間", "経過時間(+秒)", "なし", "なし(+秒)"}),
				new Input("名前表示", "名前表示", new String[] {"なし", "アカウント名", "名前", "アカウント/名前"}),
				new Input("現在地表示", "現在地表示", true),
				new Input("クライアント表示", "クライアント表示", true),
				new Input("受信件数", "受信件数", 50, 200),
				//new Input("言及件数", "言及件数", 50, 200),
				/*new Input(),new Input("test"),
				new Input("あ", "あ", "defo", 4),*/
				new Input("保存すると次回起動時もこの設定を使います。"),
				new Input("保存", 19),
				new Input("閉じる", 20)
		};
		menuimg = MediaManager.getImage("resource:///checkbox_min.gif");
		try {
			menuimg.use();
		} catch (Exception e) {
			Tuwi.log("menuimg使えぬ");
		}
	}
	
	class Input {
		String name, source;
		//Object def; // default
		int type, length;
		String[] names;
		//Object[] values;
		
		// border line
		Input() {
			type = 0;
		}
		// label
		Input(String s) {
			type = 1;
			name = s;
		}
		// checkbox
		Input(String s, String n, boolean d) {
			type = 10;
			source = s;
			name = n;
			if(!Tuwi.conf.containsKey(s))
				Tuwi.conf.put(s, new Boolean(d));
		}
		// text(String)
		Input(String s, String n, String d, int l) {
			type = 20;
			source = s;
			name = n;
			if(!Tuwi.conf.containsKey(s))
				Tuwi.conf.put(s, d);
			length = l;
		}
		// text(0以上の整数)
		Input(String s, String n, int d, int max) {
			type = 21;
			source = s;
			name = n;
			if(!Tuwi.conf.containsKey(s))
				Tuwi.conf.put(s, new Long(d));
			length = max;
		}
		// list
		Input(String s, String n, String[] ns/*, Object[] v*/) {
			type = 30;
			source = s;
			name = n;
			names = ns;
			//values = v;
		}
		// button
		Input(String n, int ev) {
			type = 40;
			name = n;
			length = ev;
		}
	}
	
	public void render(Box b) {
		// 余白の設定
		b.setWidth(5, Box.auto, 5).str(0, "");
		
		int selected = 0xffE8F2FE;
		int raw = (mc.getHeight() - b._y) / (b._h * 2 + 4);
		
		if(raw == 0) ++raw;
		//Tuwi.log("stage1");
		// 設定項目
		int i = index / raw * raw; // 整数切り捨てを期待
		for(; i < SETS.length; ++i) {
			Box row = b.child().setWidth(0, Box.auto, 0).setHeight(2, b._h*2, 2).rgba2(index == i, 0, selected).fill();
			//Tuwi.log("a"+SETS[i].type);
			switch (SETS[i].type) {
			case 0:  // border
				row.y += b._h/2-1;
				row.setHeight(0, 2, 0).rgb(0).fill();
				break;
			case 1:  // label
				row.str(0, SETS[i].name);
				break;
			case 10: // checkbox
				int j = 0, k = 0;
				if(index == i)
					j = 19;
				if(Tuwi.conf.bool(SETS[i].source))
					k = 19;
				row.img(menuimg, 5, row._y-1, b._h/2+1, b._h/2+1, j, k, j+19, k+19);
				row.str(0, "   "+SETS[i].name);
				break;
			case 20: // text
			case 21:
				row.str(0, SETS[i].name).newLine()
				 .button(0, Tuwi.conf.str(SETS[i].source), 0, 0);
				break;
			/*case 21:
				row.str(0, SETS[i].name).newLine()
				 .button(0, Tuwi.conf.str(SETS[i].source), 0, 0);
				break;*/
			case 30: // list
				//Tuwi.log("list"+SETS[i].name+(int)Tuwi.conf.Long(SETS[i].source));
				//Tuwi.log(SETS[i].names[(int)Tuwi.conf.Long(SETS[i].source)]);
				row.str(0, SETS[i].name).newLine()
				 .button(0, SETS[i].names[(int)Tuwi.conf.Long(SETS[i].source)], 0, 0).str(0, "▼");
				break;
			case 40: // button
				row.button(0, SETS[i].name, 0, 0);
				break;
			}
			
			b.newLine();
			if(b._y > mc.getHeight()) break;
		}
		
		drawScrollBar(SETS.length, index / raw * raw, i);
	}

	public boolean handleEvent(int ev, Object o) {
		switch (ev) {
		case 1: // onfocus
			onKeyPress[Display.KEY_UP] = 16;
			onKeyPress[Display.KEY_DOWN] = 17;
			onKeyRepeat[Display.KEY_UP] = 16;
			onKeyRepeat[Display.KEY_DOWN] = 17;
			onKeyRelease[Display.KEY_SELECT] = 18;
			mc.setSoftLabel(Frame.SOFT_KEY_1, null);
			mc.setSoftLabel(Frame.SOFT_KEY_2, null);
			break;
		case 16: // 上
			if(--index < 0) index = 0;
			break;
		case 17: // 下
			if(++index >= SETS.length) --index;
			break;
		case 18: // 確定
			if(index < SETS.length) {
				Input in = SETS[index];
				switch (in.type) {
				case 10: // checkbox
					Tuwi.conf.put(in.source, new Boolean(!Tuwi.conf.bool(in.source)));
					break;
				case 20: // text
					if(in.length == 0)
						in.length = com.nttdocomo.ui.TextBox.INPUTSIZE_UNLIMITED;
					mc.imeInput(this, 30, 0, Tuwi.conf.str(in.source), com.nttdocomo.ui.TextBox.KANA, in.length);
					break;
				case 21: // text integer
					mc.imeInput(this, 31, 0, Tuwi.conf.str(in.source), com.nttdocomo.ui.TextBox.NUMBER, 3);
					break;
				case 30: // list
					if((ev = (int)Tuwi.conf.Long(in.source) + 1) >= in.names.length)
						ev = 0;
					Tuwi.conf.put(in.source, new Long(ev));
					break;
				case 40: // button
					handleEvent(in.length);
					break;
				}
			}
			mc.setFonts();
			break;
		case 19: // 保存・閉じる
			Tuwi.saveConf();
			new Popup("アプリ設定を保存しました。", 3000);
		case 20:
			Tuwi.closeTab(this);
			break;
		case 30:
			Tuwi.conf.put(SETS[index].source, o);
			break;
		case 31:
			try {
				ev = (int)Long.parseLong((String)o);
				if(0 <= ev && ev <= SETS[index].length)
					Tuwi.conf.put(SETS[index].source, new Long(ev));
				else
					new Popup("0から"+SETS[index].length+"の範囲でお願いします", 5000);
			} catch (Exception e) {
				new Popup("0以上の整数を入力してください", 5000);
			}
			break;
		default:
			break;
		}
		mc.repaint();
		return false;
	}
	
	public String toString() {
		return "AppConfig";
	}
}
