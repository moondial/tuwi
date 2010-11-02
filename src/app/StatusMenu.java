package app;
import models.Element;
import models.Status;
import models.User;


import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.Display;

public class StatusMenu extends View {
	static String[] status = {"閉じる →", "これに返信", "ReTweet", "公式RT", null, "ふぁぼる/取消", null/*RT元/返信追跡*/, "コピー", null, "消す"},
					user = {"閉じる ←", "宛書く(＠)", "ﾀﾞｲﾚｸﾄﾒｯｾｰｼﾞ", "過去のつぶやき", "ふぁぼり一覧", "フォロー開始", "フォロー終了", "(以下未実装)縁切り", "ｺﾉ迷惑業者ｦ通報", "フォロー", "フォロワー", "フォロー中のリスト", "フォローされてるリスト"};
	String[][] items;
	
	View parent;
	Element elm;
	Status s;
	User u;
	int numitems, index, pos;
	
	StatusMenu(View p, Element e) {
		parent = p;
		account = p.account;
		elm = e;
		if(elm instanceof Status) {
			s = (Status)elm;
			u = s.user;
		}
		items = new String[][]{status, user};
		show();
	}
	
	StatusMenu(View p, String usr) {
		parent = p;
		account = p.account;
		items = new String[][]{user};
		u = User.parse(new Object[] {new Long(0), usr});
		show();
	}
	
	public void show() {
		MainCanvas.popupPolicy = true;
		onKeyPress[Display.KEY_IAPP] = 16;
		onKeyPress[Display.KEY_UP] = 17;
		onKeyPress[Display.KEY_DOWN] = 18;
		onKeyRepeat[Display.KEY_UP] = 17;
		onKeyRepeat[Display.KEY_DOWN] = 18;
		onKeyPress[Display.KEY_LEFT] = 19;
		onKeyPress[Display.KEY_RIGHT] = 20;
		onKeyRepeat[Display.KEY_LEFT] = 19;
		onKeyRepeat[Display.KEY_RIGHT] = 20;
		for(int i=0; i<10; ++i)
			onKeyPress[i] = 0x100 + i;
		// 最優先表示
		MainCanvas.popups.insertElementAt(this, 0);
		MainCanvas.self.repaint();

		Tuwi.log("StatusMenu created.");
	}
	
	public void close() {
		MainCanvas.popups.removeElement(this);
		MainCanvas.self.repaint();
		Tuwi.log("Popup " + getClass() + " を閉じました");
	}
	
	public void render(Box b) {
		int selected = 0x6B8E23, link = 0x2F4F4F;
			b = new Box(mc.getWidth() - lineHeiL*9, (mc.getHeight() - lineHeiL * 10) / 2,
					lineHeiL*9, lineHeiL*10);
			b.rgba(0xaaccddcc).fill();
			numitems = 0;
			for(int i=0; i<items[index].length; ++i) {
				if(items[index][i] != null) {
					if(pos==numitems++) {
						b.str(selected, items[index][i], 0, Box.LARGER);
						onKeyPress[Display.KEY_SELECT] = 0x100 + i;
					} else {
						b.str(link, items[index][i], 0, Box.LARGER);
					}
					
				}
				b.newLine();
			}
	}

	public boolean handleEvent(int ev, Object o) {
		URLUtils res;
		Tuwi.log("StatusMenu view ev" + ev);
		switch (ev) {
		case 16: // クリア
		case 0x30:
		case 0x40:
			close();
			return false;
		case 17: // ↑移動
			if(--pos < 0) pos = numitems - 1;
			break;
		case 18: // 下移動
			if(++pos >= numitems) pos = 0;
			break;
		case 19: // 左タブへ
			if(--index < 0) index = items.length - 1;
			break;
		case 20: // 右タブへ
			if(++index >= items.length) index = 0;
			break;
			
		/**
		 * 0x3x: status menu
		 */
		case 0x31: // 1.reply
			Tuwi.openTab(new UpdateForm(account, "@"+u.screen_name+" ", (Status)elm, true));
			break;
		case 0x32: // 2.ReTweet
			Tuwi.openTab(new UpdateForm(account,
				" RT @"+u.screen_name+": "+s.text, (Status)elm, false));
			break;
		case 0x33: // 3.公式RT
			if(Tuwi.dialog(Dialog.DIALOG_INFO | Dialog.DIALOG_YESNO,
					"Retweet確認", "公式Retweetしますか") == Dialog.BUTTON_YES) {
				URLUtils.createThread(this, 0x3c, null);
			}
			break;
		case 0x34: // 4.
			break;
		case 0x35: // 5.(fav|unfav) it
			if(s.isFaved())
				URLUtils.createThread(this, 0x3e, null);
			else
				URLUtils.createThread(this, 0x3d, null);
			break;
		case 0x36: // 6.
			break;
		case 0x37: // 7.copy
			mc.imeInput(this, 0, 0, s.toString(), com.nttdocomo.ui.TextBox.KANA, com.nttdocomo.ui.TextBox.INPUTSIZE_UNLIMITED);
			break;
		case 0x38: // 8.
			break;
		case 0x39: // 9.remove universe
			// ひとのものはけさせない！
			if(!u.screen_name.equals(account.user.screen_name))
				break;
			if(Tuwi.dialog(Dialog.DIALOG_INFO | Dialog.DIALOG_YESNO,
					"削除確認", "本当に削除してもよろしいでしょうか？後悔しませんね？") == Dialog.BUTTON_YES) {
				URLUtils.createThread(this, 0x3f, null);
			}
			break;
		case 0x3c:
			res = URLUtils.APIRequest(account, "retweet", "i=" + elm.id);
			if (res.code == 200) {
				new Popup("リツイート成功です", 5000);
			} else {
				new Popup("HTTP"+res.code+res.msg, 5000);
			}
			break;
		case 0x3d:
			res = URLUtils.APIRequest(account, "fav", "i=" + elm.id);
			if (res.code == 200) {
				Tuwi.log(res.msg.toString());
				s.overwrite(Status.parse((Object[])res.msg));
				new Popup("ふぁぼりました", 5000);
			} else {
				new Popup("HTTP"+res.code+res.msg, 5000);
			}
			break;
		case 0x3e:
			res = URLUtils.APIRequest(account, "unfav", "i=" + elm.id);
			if (res.code == 200) {
				s.overwrite(Status.parse((Object[])res.msg));
				// TODO:atteru?
				//s.flags1 &= 0xffff - 2;
				new Popup("取り消しました", 5000);
			} else {
				new Popup("HTTP"+res.code+res.msg, 5000);
			}
			break;
		case 0x3f:
			res = URLUtils.APIRequest(account, "destroy", "i=" + elm.id);
			if (res.code == 200) {
				new Popup("削除できました", 5000);
				s.setText("(削除済)");
			} else {
				new Popup("HTTP"+res.code+res.msg, 5000);
			}
			break;
		/**
		 * 0x4x: user menu
		 */
		case 0x41: // 1.tweet with @
			Tuwi.openTab(new UpdateForm(account, "@"+u.screen_name+" ", (Status)elm, false));
			break;
		case 0x42: // 2.send directmessage
			Tuwi.openTab(new UpdateForm(account, "D "+u.screen_name+" ", (Status)elm, false));
			break;
		case 0x43: // 3.show black history
			Tuwi.openTab(new TimelineView(account,
					""+u.getName().substring(0, 6),
					"userTL?u="+u.screen_name)
			);
			break;
		case 0x44: // 4.show favori
			Tuwi.openTab(new TimelineView(account,
					"☆"+u.getName().substring(0, 6),
					"favorites?u="+u.screen_name)
			);
			break;
		case 0x45: // 5.follow
			URLUtils.createThread(this, 0x4c, null);
			break;
		case 0x46: // 6.unfollow
			URLUtils.createThread(this, 0x4d, null);
			break;
		case 0x4c:
			res = URLUtils.APIRequest(account, "follow?screen_name=" + u.screen_name, "");
			if (res.code == 200) {
				new Popup("フォロー開始しました", 5000);
			} else {
				new Popup("HTTP"+res.code+res.msg, 5000);
			}
			break;
		case 0x4d:
			res = URLUtils.APIRequest(account, "unfollow?screen_name=" + u.screen_name, "");
			if (res.code == 200) {
				new Popup("フォロー終了しました", 5000);
			} else {
				new Popup("HTTP"+res.code+res.msg, 5000);
			}
			break;
		
		case 0x100:
		case 0x101:
		case 0x102:
		case 0x103:
		case 0x104:
		case 0x105:
		case 0x106:
		case 0x107:
		case 0x108:
		case 0x109:
			if(items[index] == status)
				handleEvent(ev-0xD0); // 0x30-0x3b
			if(items[index] == user)
				handleEvent(ev-0xC0); // 0x40-0x4b
			close();
			return false;
		}
		mc.repaint();
		
		// イベントを下層へ渡さない
		return false;
	}
}
