package app;
import models.Account;
import models.Status;


import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.Display;

public class UpdateForm extends View {
	int i/*, t*/;
	boolean isrep;  // in reply to status id を付加するかどうか
	String text;
	Status s;  // 元つぶやき
	/**
	 * 投稿タブ
	 * @param a 投稿者アカウント
	 * @param t デフォ内容
	 * @param st 元つぶやき
	 * @param isreply in reply to status id を付加するかどうか
	 */
	UpdateForm(Account a, String t, Status st, boolean isreply) {
		super();
		title = a.userid();
		account = a;
		onKeyRelease[Display.KEY_UP] = 16;
		onKeyRelease[Display.KEY_DOWN] = 17;
		onKeyRelease[Display.KEY_SELECT] = 18;
		if(t == null)
			text = "";
		else
			text = t;
		s = st;
		isrep = isreply;
		handleEvent(18);
	}
	
	UpdateForm(Account a) {
		this(a, null, null, false);
	}
	
	public void render(Box b) {
		//System.out.println(t % 20);
		//b.str(0xaa3333, "使用アカウント: ".concat(account.userid()), 0xeeccee, 0).skipLine();
		
		b.str(0, "本文 ");
		int count = 140 - text.length();
		if(count > 0)
			b.str(0x006400, "残り " + count + "字");
		else if(count == 0)
			b.str(0x0000ee, "ぴったり140字");
		else
			b.str(0xff2222, "超過 " + -count + "字");
		b = b.newLine()
		.child()
		.setWidth(0, b.boxWidth() - 1, 0)
		.setHeight(0, mc.getHeight() - b._y - lineHei*3, 0)
		.rect()
		.setWidth(0, Box.auto, 0);
		
		if(count >= 0)
			b.str(0, text);
		else {
			b.str(0, text.substring(0, 140))
			.str(0xff2222, text.substring(140), 0, 4);
		}
		if(i == 0)
			b.child().setWidth(0, 2, 0).setHeight(0, lineHei, 0).rgb(0).fill();
		b.parent().newLine().button(i==1?0xFF6347:0, "つぶやく", 0, 0)
		.newLine()
		.str(0, "宛先ステータスID: ")
		.button(i==2?0xFF6347:0, s != null? ""+s.id: "なし", 0, isrep? 0: Box.MIDDLELINE);
	}
	
	public boolean handleEvent(int ev, Object o) {
		switch(ev) {
		case 1: // focus
			break;
		case 2: // unfocus
			break;
		case 3: // timer
			/*if(i==0) {
				++MainCanvas.timer_expire;
				if(++t % 10 != 0) return true;
			}*/
			break;
		case 16:
			if(--i < 0) i = 2;
			//t = 0;
			//if(i == 1) i = -19*2;
			onKeyRelease[Display.KEY_SELECT] = i==1?-19:18 + i;
			break;
		case 17:
			if(++i > 2) i = 0;
			//t = 0;
			onKeyRelease[Display.KEY_SELECT] = i==1?-19:18 + i;
			break;
		case 18: // input
			mc.imeInput(this, 30, 0, text, com.nttdocomo.ui.TextBox.KANA, com.nttdocomo.ui.TextBox.INPUTSIZE_UNLIMITED);
			break;
		case 19:
			// TODO:urlの公式bit.ly圧縮を前提とした文字数カウント
			// TODO:自動トリム
			if (text.trim().equals("") || text.length() > 140) {
				new Popup("140字以上あります", 5000);
				return true;
			}
			Popup p = new Popup("ポスト中...");
			// ここから投稿処理
			String[] q = new String[]{"status", text, "r", null};
			if(isrep) q[3] = String.valueOf(s.id);
			
			URLUtils res = URLUtils.APIRequest(account, "update", URLUtils.urlencode(q));
			// 200 OK
			if (res.code == 200) {
				try {
					account.user = new Status((Object[])res.msg).user;
				} catch (Exception e) {
					e.printStackTrace();
					Tuwi.log("無効なユーザー情報です");
				}
				try {
					Tuwi.dialog(Dialog.BUTTON_OK | Dialog.DIALOG_INFO, "投稿成功", "やったね！！！");
					Tuwi.closeTab(this);
				} catch (Exception e) {
					e.printStackTrace();
					Tuwi.log(e);// TODO: handle exception
				}
			} else
				Tuwi.dialog(Dialog.BUTTON_OK | Dialog.DIALOG_INFO, "投稿失敗", "もう一度投稿してみてください。");
			p.close();
			break;
		case 20:
			// in reply to status id 付加/無視 切り替え
			isrep = !isrep;
			break;
		case 30:
			text = (String)o;
			break;
		}
		//Tuwi.log(""+ev);
		mc.repaint();
		return true;
	}
	
	
}
/*class Dummy extends Inputb.stream{
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
		System.out.println(Integer.toHexstring((int)(Double.doubleToLongBits(0.0005d)&0xffffffff)));
		
		return data[count++];
		
	}
	
}*/