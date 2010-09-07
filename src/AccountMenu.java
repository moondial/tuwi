import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;

public class AccountMenu extends View {
	// TODO presetをつかってカスタマイズ
	// preset 要件 どんなタブか・タブ順序・タブの内容(アカウント、タブ名、URL、縦位置、えｔｃ)
	// View#packunpack(ExHash or null):Object[]を実装
	static Element[] VIEWS = {
		new Element(101, 0, "home timeline"),
		new Element(102, 0, "mentions"),
		new Element(103, 0, "your tweets"),
		new Element(104, 0, "favorites"),
		new Element(120, 0, "システムログ"),
		new Element(105, 0, "list"),
		new Element(107, 0, "search(本家)"),
		new Element(100, 0, "public timeline"),
		new Element(106, 0, "DM受信箱"),
		new Element(109, 0, "検索(pcod)"),
		new Element(111, 0, "日本語圏TL"),
	};
	
	int m/*画面状態 0:機能選択, 1:アカウント選択, 2:アカウント設定 */,
	f/*フォーカス*/,
	fu/*フォーカスしたユーザ*/,
	fi/*フォーカスしたアイテム*/,
	index, fh;
	
	AccountMenu(Account a) {
		super();
		account = a;
		title = a.user.screen_name;
		// アカウントを先頭に移動
		a.to1st();
		handleEvent(1);
	}
	
	public void render(Box b) {
		// 余白の設定
		b.setWidth(5, Box.auto, 5);
		// 名前
		b.str(0, account.userid()).newLine();
		
		fh = b._h;
		int selected = 0xffE8F2FE;
		int raw = (mc.getHeight() - b._y) / (fh * 2 + 4) ;
		
		for(int i = index / raw * raw; // 整数切り捨てを期待
				i < VIEWS.length; ++i) {
			b.child().setHeight(2, fh, 2).rgba2(index == i, 0, selected).fill()
			.str(0, VIEWS[i].text_chunk[0]);
			b.newLine();
			if(b._y > mc.getHeight()) return;
		}
		/*Account a;
		g.setOrigin(0, 0);
		Box b = new Box(0, _y, mc.getWidth(), Box.auto);
		//b.child().setWidth(16, Box.auto, 16).setHeight(10, 100, 10).rgba(0xffffffff).fill();
		//b.str(0, "testああいいいいいいいいいいいいいえええええええあああああああt");
		b.child().str(0,"→ボタン").button(0, "longlonglonglongああaえええええええええええああ", 0, 0).button(0, "test", 0xffffffff, 0)
		.button(0, "longlonglonglongああaえええええええええええああ", 0, 0).button(0, "test", 0xffffffff, 0);
		//drawlink(b, 60, "test", "testlink!", true);
		if(m == 0) {
			g.setOrigin(0, -y);
			xx = 20;
			str(0, "Tuwi v".concat(Tuwi.version)).skipLine();
			str(0xff, account.userid()).skipLine();
			String[] s = {"home timeline", "mentions", "your tweets", "favorites", "システムログ","list","public timeline","DM受信箱"};
			for(int j=0; j<s.length; ++j) {
				if(f == j)
					str(0, s[j], 0xF3F8FE, 1);
				else
					str(0, s[j]);
				skipLine();
			}
			/*str(f==5?0xFF6347:0, "ユーザ名");
			g.drawRect(_x, _y, mc.getWidth() - _x - 5, lineHei);
			_x += 3;
			str(0, account.screen_name).skipLine();
			str(f==6?0xFF6347:0, "パスワード変更").skipLine();
			* /
			str(f==8?0xFF6347:0, "受信件数: ".concat(String.valueOf(account.get_count))).skipLine();
			int j = _x;
			_y += 2;
			str(f==9?0xFF6347:0, "保存");
			g.drawRect(j - 2, _y - 2, _x - j + 4, lineHei + 4);
			skipLine();
			if(f == 10)
				str(0xFF6347, "アプリ設定", 0xF3F8FE, 1);
			else
				str(0, "アプリ設定");
			skipLine();
			str(0, "不具合報告、感想等は @moondial0 までお寄せください。").skipLine();
			str(0x882222, "つぶやく時は＊キー！\nｱｶｳﾝﾄ切替は右上のボタン");
			str(0, "API "+account.api[0]+"/"+account.api[1]);
		}
		if(m == 1 || m == 2) {
			str(0, "アカウント一覧").skipLine();
			
			int l = Account.size();
			for(int j=0; j<=l; j++) {
				if(fu == j) rgba(0xffE8F2FE);
				else if (j % 2 == 1) rgba(0x0);
				else rgba(0x7fE8F2FE);
				g.fillRect(0, _y, mc.getWidth(), lineHei * 2);
				if(j == l)
					str(0, "アカウント追加");
				else {
					a = Account.at(j);
					str(0, a.userid()).skipLine();
					str(0, a.get_count + " " + a.read_id).skipLine();
					if(fu == j && m == 2) {
						a = Account.at(fu);
						str(fi == 0? 0xFF6347:0, "  >>アカウント削除").skipLine();
						str(fi == 1? 0xFF6347:0, "  >>閉じる").skipLine();
					}
				}
			}
		}
		height = _y;*/
	}
	
	public boolean handleEvent(int ev, Object o) {
		//Tuwi.log("TopMenu"+ev);
		switch(ev) {
		case 1: // focus
			if(account == null) {
				handleEvent(30);
				return false;
			}
			// ソフトキーのラベルを設定
			mc.setSoftLabel(Frame.SOFT_KEY_1, null);
			mc.setSoftLabel(Frame.SOFT_KEY_2, null);
			m = 0;
			onKeyRelease[Display.KEY_UP] = 16;
			onKeyRelease[Display.KEY_DOWN] = 17;
			onKeyRepeat[Display.KEY_UP] = 16;
			onKeyRepeat[Display.KEY_DOWN] = 17;
			onKeyRelease[Display.KEY_SELECT] = -18;
			onKeyPress[Display.KEY_ASTERISK] = 23;
			onKeyPress[Display.KEY_SOFT1] = 0;
			onKeyPress[Display.KEY_SOFT2] = 30;
			
			onKeyPress[Display.KEY_POUND] = 99;
			break;
		case 2: // unfocus
			break;
		case 16:
			if(--index < 0) index = 0;
			break;
		case 17:
			if(++index >= VIEWS.length) index--;
			break;
		case 18:  // open home timeline
			handleEvent((int)VIEWS[index].id);
			break;
		/*case 19:
			System.out.println("IME Canceled: ".concat(o.toString()));
			break;
		case 20:
			//account.screen_name = (String)o;
			break;
		case 21:
			account.password = (String)o;
			break;
		case 22:
			account.get_count = Integer.parseInt((String)o);
			break;
		
		case 30: // アカウント切り替え
			m = 1;
			mc.setSoftLabel(Frame.SOFT_KEY_1, "変更");
			onKeyPress[Display.KEY_SOFT1] = 40;
			onKeyPress[Display.KEY_SOFT2] = onKeyPress[Display.KEY_IAPP] = 1;
			onKeyRelease[Display.KEY_UP] = 31;
			onKeyRelease[Display.KEY_DOWN] = 32;
			onKeyRelease[Display.KEY_SELECT] = 33;
			break;
		case 31:
			if(--fu < 0) fu = Account.size();
			break;
		case 32:
			if(++fu > Account.size()) fu = 0;
			break;
		case 33: // アクティブアカウント変更 or 作成
			// アカウント作成
			if(fu == Account.size()) {
				Tuwi.self.openURI(Tuwi.BASE + "static/iappli/oauth.html", null);
			} else {
				Tuwi.conf.put("accountIndex", new Integer(fu));
				account = Account.at(fu);
			}
			handleEvent(1);
			break;
			
		case 40: // アカウント設定
			// 無視する
			if(fu >= Account.size()) break;
			m = 2;
			mc.setSoftLabel(Frame.SOFT_KEY_1, "中止");
			mc.setSoftLabel(Frame.SOFT_KEY_2, "戻る");
			onKeyPress[Display.KEY_SOFT1] = onKeyPress[Display.KEY_IAPP] = 30;
			onKeyPress[Display.KEY_SOFT2] = 1;
			onKeyRelease[Display.KEY_UP] = 41;
			onKeyRelease[Display.KEY_DOWN] = 42;
			onKeyRelease[Display.KEY_SELECT] = 43;
			break;
		case 41:
			if(--fi < 0) fi = 1;
			break;
		case 42:
			if(++fi > 1) fi = 0;
			break;
		case 43: // アカウント設定で選択
			if(fi == 0) {
				if(Dialog.BUTTON_YES == Tuwi.dialog(
						Dialog.DIALOG_WARNING | Dialog.DIALOG_YESNO, "警告",
						Account.at(fu).userid() + " を端末から削除します。よろしいですか？\n(Twitterアカウント自体は削除されません)")
				&& Dialog.BUTTON_YES == Tuwi.dialog(
						Dialog.DIALOG_ERROR | Dialog.DIALOG_YESNO, "本当に？？",
						"本当に " + Account.at(fu).userid() + " を端末から削除します。\n後　悔　し　ま　せ　ん　ね　？")
				){  // 後悔してないらしいのでｻｸｼﾞｮｫｫｫ(デスノート風に
					Account.getAccounts().removeElementAt(fu);
					Tuwi.dialog(Dialog.DIALOG_INFO, "削除しました", "この後面倒でないのならTwitter本家から今のアカウントのOAuth設定を解除してください。\nなお、今のアカウントで開かれていたタブは終了するまで残ります。");
					Tuwi.saveConf();
				}
			}
			if(fi == 1) {  // 閉じる
				handleEvent(30);
				return false;
			}
			handleEvent(1);
			break;
		case 50:
			
			break;*/
		case 23:
			Tuwi.openTab(new UpdateForm(account));
			break;
		case 100:
			Tuwi.openTab(new TimelineView(account, "公開TL", "publicTL?"));
			break;
		case 101:
			Tuwi.openTab(new TimelineView(account, "ホーム", "h?"));
			break;
		case 102:
			Tuwi.openTab(new TimelineView(account, "言及", "m?"));
			break;
		case 103:
			Tuwi.openTab(new TimelineView(account, ""+account.userid(), "U?"));
			break;
		case 104:
			Tuwi.openTab(new TimelineView(account, "☆"+account.userid(), "f?"));
			break;
		case 105:
			mc.imeInput(this, 110, 19, null, com.nttdocomo.ui.TextBox.ALPHA, 15);
			break;
		case 110:
			Tuwi.openTab(new TimelineView(account, (String)o, "listTL?slug="+o+"&"));
			break;
		case 106:
			Tuwi.openTab(new TimelineView(account, "DM受", "DM?"));
			break;
		case 107:
			mc.imeInput(this, 108, 19, null, com.nttdocomo.ui.TextBox.KANA, 30);
			break;
		case 108:
			Tuwi.openTab(new TimelineView(account, ""+o, "search?"+URLUtils.urlencode(new String []{"q", (String)o})));
			break;
		case 109:
			mc.imeInput(this, 112, 19, null, com.nttdocomo.ui.TextBox.KANA, 30);
			break;			
		case 112:
			Tuwi.openTab(new TimelineView(account, "_"+o, "JPSearch?"+URLUtils.urlencode(new String []{"q", (String)o})));
			break;
		case 111:
			Tuwi.openTab(new TimelineView(account, "日本語公開TL", "JPPublicTL?"));
			break;
		case 120:
			Tuwi.openTab(new LogView());
			break;
		}
		// スクロールバー
		//height = (VIEWS.length-1) * (fh * 2 + 4) + mc.getHeight();
		//y = index * (fh * 2 + 4);
		//Tuwi.log(""+ev);
		mc.repaint();
		return true;
	}
	
	public String toString() {
		return "" + account.hashCode();
	}
}
/*class Dummy extends InputStream{
	private int count=0;
	private int[] data = new int[]{
			0x99,
			 0xc0,
			 0xc2,
			 0xc3,
			 0xca, 0x3a, 0x03, 0x12, 0x6f,
			 0xcb, 0x3f, 0x40, 0x62, 0x4d, 0xd2, 0xf1, 0xa9, 0xfc,
			 0xcc, 200,
			 0xcd, 0xff, 0x00,
			 0xce, 0xff, 0xff, 0xff, 0xff,
			 0xcf, 0x3f, 0x40, 0x62, 0x4d, 0xd2, 0xf1, 0xa9, 0xfc,
			 };
	public int read() throws IOException {
		System.out.println(Integer.toHexString((int)(Double.doubleToLongBits(0.0005d)&0xffffffff)));
		// TODO 自動生成されたメソッド・スタブ
		return data[count++];
		
	}
	
}*/