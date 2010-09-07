import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;

public class Frontpage extends View {
	private int index, fh;
	
	public Frontpage() {
		super();
		title = "◆";
		titlecolor = 0xff0000;
		// titledeco = 0;
		onKeyPress[Display.KEY_DOWN] = 16;
		onKeyPress[Display.KEY_UP] = 17;
		onKeyRepeat[Display.KEY_DOWN] = 16;
		onKeyRepeat[Display.KEY_UP] = 17;
		onKeyRelease[Display.KEY_SELECT] = 18;
		onKeyPress[Display.KEY_ASTERISK] = 19;
		handleEvent(1);
	}
	
	// TODO: もっとちゃんとしたUIのならべかた、巨大すぎる文字設定
	public void render(Box b) {
		// 余白の設定
		b.setWidth(5, Box.auto, 5);
		// ロゴ
		b.str(0, "Tuwi v".concat(Tuwi.version)).newLine();
		fh = b._h;
		int selected = 0xffE8F2FE;
		int raw = (mc.getHeight() - b._y) / (fh * 2 + 4) ;
		int i = index / raw * raw;  // 整数切り捨てを期待
		
		// アカウント
		for(;i < Account.size(); ++i) {
			account = Account.at(i);
			b.child().setHeight(2, fh*2, 2)
			.rgba2(index == i, 0, selected).fill()
			.img(account.user.getIcon(), fh*2, fh*2)
			.child()
			.str(0, account.userid()).newLine()
			.str(0, "このｱｶｳﾝﾄを開く"+account.api[0]+"/"+account.api[1]);
			b.newLine();
			if(b._y > mc.getHeight()) {
				drawScrollBar(Account.size() + 2, index / raw * raw, i);
				return;
			}
		}
		
		// アカウント追加
		b.child().setHeight(fh/2, fh, fh/2).rgba2(index == Account.size(), 0, selected).fill().str(0, "アカウント追加");
		if(b.newLine()._y <= mc.getHeight()) ++i;
		
		// アプリ設定
		b.child().setHeight(fh/2, fh, fh/2).rgba2(index == Account.size()+1, 0, selected).fill().str(0, "アプリ設定");
		if(b.newLine()._y <= mc.getHeight()) ++i;
		
		drawScrollBar(Account.size() + 2, index / raw * raw, i);
	}

	// TODO:アカウント削除　右S、「アカウント削除」で決定、左S、決定、決定。
	
	public boolean handleEvent(int ev, Object o) {
		//Tuwi.log("i:"+index);
		switch (ev) {
		case 1:
			mc.setSoftLabel(Frame.SOFT_KEY_1, null);
			mc.setSoftLabel(Frame.SOFT_KEY_2, null);
			break;
		case 16: // ↓
			if(index++ > Account.size()) index--;
			break;
		case 17: // ↑
			if(--index < 0) index = 0;
			break;
		case 18: // 決定
			if(index < Account.size()) { // open account's menu
				Tuwi.openTab(new AccountMenu(Account.at(index)));
			} else if(index == Account.size())  // Add Account
				Tuwi.self.openURI(Tuwi.BASE + "static/iappli/oauth.html", null);
			else  // appconfig
				Tuwi.openTab(new AppConfig());
			break;
		case 19:
			if(index < Account.size())
				Tuwi.openTab(new UpdateForm(Account.at(index)));
			break;
		default:
			break;
		}
		mc.repaint();
		return true;
	}
	
}
