package app;


import java.util.Vector;

import models.Account;
import models.DirectMessage;
import models.Element;
import models.Retweet;
import models.RichText;
import models.Status;
import models.User;


import com.nttdocomo.ui.Display;

class TimelineView extends View {
	public static int MAX_SCROLL_SPEED = 500; // 加速装置
	public String url;
	public Vector data = new Vector(400);
	/**
	 * @param index 描画開始要素 == data[index]
	 * @param y index描画開始時のy位置（下向きを正に取る）
	 * @param focus_at 選択中の要素 == data[focus_at]
	 * @param link_at 選択中のリンクの番号
	 * @param scroll_level 必死だな（藁
	 *
                 ,´ _,, '-´￣￣｀-ゝ 、_ イ、
                'r ´          ヽ、ﾝ、
                ,'＝=─-      -─=＝', i
                i ｲ iゝ、ｲ人レ／_ルヽｲ i |
                ﾚﾘｲi (ﾋ_]     ﾋ_ﾝ ).| .|、i .||
                 !Y!""  ,＿__,   "" 「 !ﾉ i |
                 L.',.   ヽ _ﾝ    L」 ﾉ| .|
                 | ||ヽ、       ,ｲ| ||ｲ| /
                 レ ル｀ ー--─ ´ルﾚ ﾚ´

                  ┌──────┐
                  │ 1玉  ￥150 │
                  └──────┘
	 */
	public int index, y, focus_at, link_at, scroll_level, last;

	TimelineView(Account a, String n, String u) {
		account = a;
		title = n;
		url = u;
		onKeyPress[3] = 22;
		onKeyPress[4] = 27;
		onKeyPress[9] = 23;
		onKeyPress[Display.KEY_ASTERISK] = 30;
		onKeyPress[Display.KEY_UP] = 20;
		onKeyPress[Display.KEY_DOWN] = 21;
		onKeyRepeat[Display.KEY_UP] = 20;
		onKeyRepeat[Display.KEY_DOWN] = 21;
		onKeyRelease[Display.KEY_SELECT] = -42;
		onKeyRelease[Display.KEY_SOFT1] = -40;
		onKeyRelease[Display.KEY_SOFT2] = -41;
		handleEvent(1);
	}

	public void render(Box b) {
		try {
		b._y -= y;
		//Tuwi.log(y+" "+b._y);
		// 余白の設定
		b.setWidth(0, Box.auto, 0);
		// 名前
		//b.str(0, account.userid()).newLine();

		//int selected = 0xffE8F2FE;
		//int raw = (mc.getHeight() - b._y) / (b._h * 2 + 4) ;
		//b.str(0, URLUtils.loadedBytes + "bytes 計" + Tuwi.conf.Long("dlBytes") / 1024 + "Kbytes "+data.size()+"本").newLine();
		if(data.isEmpty()) {
			b.str(0, "[決定]キー:つぶやきをダウンロード\n[4]キー:アイコン取得");
			return;
		}
		//Object focus = ((Element)data.elementAt(index)).links[link_at];
		int iconW = 0;
		if(Tuwi.conf.bool("showIcons"))
			iconW = 48;

		int i;
		for(i=index; i<data.size(); ++i) {
			Element elm;
			synchronized(data) {
				elm = (Element)data.elementAt(i);
			}
			elm.y = b._y;
			if(elm instanceof Status) {
				Status s = (Status)elm;
				/* 一行表示
				b.child().setHeight(0, lineHei, 0)
				.rgba2(i%2==0, 0x7f7f7fff, 0).fill()
				.img(s.user.getIcon(), lineHei, lineHei)
				.clip()
				.str(0, s.text)
				.parent().newLine();
				//*/
				//* 通常表示
				boolean isRT = elm instanceof Retweet;
				if(isRT)
					s = ((Retweet)s).RTed_status;
				// おしりにつけるもの
				String footer = "", date = s.getDate(), name = s.user.getName();
				if(Tuwi.conf.bool("現在地表示")) {
					if(s.user.location != null)
						footer = '[' + s.user.location + ']';
					if(isRT && ((Status)elm).user.location != null)
						footer += '[' + ((Status)elm).user.location + ']';
				}
				if(Tuwi.conf.bool("クライアント表示"))
					footer += '(' + s.source + ')';
				if(isRT)
					name += "(RT*" + ((Status)elm).user.getName() + ")";

				// 高さを予め決めておく
				int height = b.child().setWidth(iconW, Box.auto, 0)
				.measure(name, 0)
				.measure(date, Box.右揃え | Box.NOBREAK).newLine()
				.measure(s.text+footer, 0).boxHeight();
				// 縦幅は最低でもアイコン高さ以上
				if(height < iconW) height = iconW;
				((Element)elm).height = height;

				Box bb = b.child()
				.setWidth(0, Box.auto, 0)
				.setHeight(0, height, 0)
				.rgba2(i%2==0, 0x7fE8F2FE, 0).fill()
				.img(s.user.getIcon(), iconW, iconW);
				if(isRT)
					bb.newLine().img(((Status)elm).user.getIcon(), iconW, iconW)._y -= iconW;
				bb.child()
				.setWidth(0, Box.auto, 0)
				.linkstr(name, 0x566666, 0xfafafa, 0x7f566666, i == focus_at && link_at == 0)
				.str(0x566666, date, 0, Box.右揃え | Box.NOBREAK).newLine()
				.richstr(s.getSplitedText(), s.format, i == focus_at ?link_at - 1:-1)
				.str(0x776666, footer);
				b._y += height;
				//*/
				// TODO: Box.if
			}
			// 描画停止
			if(b._y > mc.getHeight()) break;
		}
		last = i;

		b.parent(); // unclip
		drawScrollBar(data.size(), index, index + 5);
		// todo: focus, click events
		} catch(Exception e) {
		}
	}

	public boolean handleEvent(int ev, Object o) {
		if(ev > 21 || ev < -21)
			Tuwi.log("TimelineView#" + ev);

		switch (ev) {
		case 1: // focus
			mc.setSoftLabel(0, "続き");
			mc.setSoftLabel(1, "新着");
			break;
		case 2:
			break;
		case 3:
			// 何かキーが押されたまま
			if (scroll_level == 0 || mc.getKeypadState() != 0)
				return false;
			++MainCanvas.timer_expire;
			// ゆっくりうごいていってね！
			scroll(0);
			break;
		case 16:
			title = "" + title;
			Popup p = new Popup("タイムライン読込中...", 0);
			URLUtils res = URLUtils.APIRequest(account, (String) o + "&c="+Tuwi.conf.str("受信件数"), null);
			title = title.substring(1);
			p.close();
			if(res.code != 200) {
				if(res.code == 400)
					new Popup("API制限のようです\n"+res.msg, 5000);
				if(res.code == 403)
					new Popup("アクセス許可がありません\n"+res.msg, 5000);
				if(res.code == 500)
					new Popup("サーバーエラー\n"+res.msg, 5000);
				if(res.code == 502)
					new Popup("サーバーダウンかメンテナンス中\n"+res.msg, 5000);
				if(res.code == 503)
					new Popup("サービス応答不能", 5000);
				if(res.code == 504)
					new Popup("接続エラー", 5000);
				if(res.code >= 1000)
					new Popup(res.msg, 5000);
				return false;
			}
			p = new Popup("並び替え中...", 0);
			try {
				if(res.meta.str("fmt").equals("DM")) {
					DirectMessage.unpack((Object[])res.msg, data);
				} else
					Status.unpack((Object[])res.msg, data);
				Tuwi.log(data.size()+"");
				synchronized(data) {
					sort(data, 0, data.size() - 1);
					// 重複削除
					for(int i=0; i<data.size()-1; ++i)
						if(((Element)data.elementAt(i)).id == ((Element)data.elementAt(i+1)).id)
							data.removeElementAt(i);
				}
				if(url.charAt(0) == 'h')
					account.read_id = Math.max(((Element)data.firstElement()).id, ((Element)data.lastElement()).id);
				Tuwi.saveConf();

				/*o = data[index];  // フォーカスを保存
				index = -1;
				quickSort(data, 0, data.length - 1, false);

				for (ev = 0; ev < data.length; ++ev) {
					data[ev].height = 0;
					if(data[ev] == o) index = ev;
				}
				if(index == -1)
					index = link_at = 0;

				first_id = Math.min(data[0].id, data[data.length - 1].id);
				latest_id = Math.max(data[0].id, data[data.length - 1].id);

				// TODO: if home>任意指定可へ
				if(url.charAt(0) == 'h') account.read_id = latest_id;
				Tuwi.saveConf();

				//* / 強制的に一度描画させる
				Tuwi.log("y: "+y+" "+data[index].chunk_ypos[link_at]+" "+index+" "+link_at);
				y = 0x40000000;
				_y = 0;
				onKeyRepeat[Display.KEY_UP] = onKeyRepeat[Display.KEY_DOWN] = 0;
				calculating = true;
				render();
				calculating = false;
				//Tuwi.log("y2 "+data[index].chunk_ypos[link_at]);
				y = data[index].chunk_ypos[link_at] - 100;
				handleEvent(22);
				// キーを戻す
				handleEvent(1);
				//*/
				// アイコンＤＬ
				if(Tuwi.conf.bool("autoIconDL"))
					URLUtils.createThread(this, 17, null);
				p.close();
				mc.repaint();
			} catch (Exception e) {
				e.printStackTrace();
				p.close();
				p = new Popup(e.toString(), 5000);
			}
			break;
		case 17: // DL icons
			p = new Popup("アイコン取得中...", 0);
			User.DLIcons(data);
			p.close();
			Tuwi.saveConf();
			p = new Popup("設定を保存しました", 1000);
			break;
		case 19:
			handleEvent(16, url);
			break;
		case 20: // 上移動
			scroll(+1);
			break;
		case 21: // 下移動
			scroll(-1);
			break;
		case 22: // 最上部
			index = y = focus_at = link_at = 0;
			mc.repaint();
			break;
		case 23: // 最下部
			index = focus_at = Math.max(0, data.size() - 1);
			y = link_at = 0;
			mc.repaint();
			break;
		case 25: // 確定
			if(data.isEmpty()) return handleEvent(42);
			o = data.elementAt(focus_at);
			if(link_at == 0) {
				if(o instanceof Retweet)
					new StatusMenu(this, ((Retweet)o).RTed_status);
				else // status
					new StatusMenu(this, (Element)o);
			} else {
				String text = ((Element)o).getSplitedText()[link_at - 1];
				switch (((Element)o).getLinkType(link_at)) {
				case RichText.LINK:
					Tuwi.self.openURI(text);
					break;
				case RichText.ATREPLY:
					new StatusMenu(this, text.substring(1));
					break;
				case RichText.HASHTAG:
					Tuwi.openTab(new TimelineView(account, ""+text, "search?"+URLUtils.urlencode(new String []{"q", text})));
				default:
					break;
				}
			}
			break;
		case 26: // メニュー閉じる
			handleEvent(1);
			mc.repaint();
			break;
		case 27: // 4icondl
			URLUtils.createThread(this, 17, null);
			break;
		case 30: // *投稿
			Tuwi.openTab(new UpdateForm(account));
			break;
		case 40: // 追加読み込み(下へ)
		case 41: // 新着読み込み(上へ)
		case 42: // 初回読み込み
			synchronized(url) {
				if(!data.isEmpty() && ev == 40) {
					handleEvent(16, url + "&m=" + ((Element)data.firstElement()).id);
				}
				if(ev == 41) {
					long id;
					if(data.isEmpty())
						id = account.read_id;
					else
						id = ((Element)data.lastElement()).id;
					//if(id == 0) id++;  // since_id=0はAPIエラーになる
					handleEvent(16, url + "&s=" + id);
				}
				if(ev == 42) {
					handleEvent(16, url);
				}
				onKeyRelease[Display.KEY_SELECT] = 0;
				onKeyPress[Display.KEY_SELECT] = 25;
			}
			break;
		}
		return false;
	}

	// キーの状態 dir = {0: なし, 1: 上, -1: 下}
	public void scroll(int dir) {
		try {
		//*** Phase0 転ばぬ先の杖
		if(data.isEmpty()) return;

		//*** Phase1 縦位置の移動
		// スクロール方向と同じキーが押されていなかったらゆっくり魔理沙
		if(dir == 0 || scroll_level * dir > 0) {
			scroll_level *= 0.8;
			if(dir == 0) {
				if(scroll_level > 0) {
					dir = -1;
				} else {
					dir = 1;
				}
			}
		} else { // ksk
			scroll_level -= dir;
		}
		y += -dir * 0.01 * scroll_level * scroll_level + 0.1 * scroll_level -dir * 5;
		if(index == 0 && y < 0) {
			scroll_level = (int)Math.abs(scroll_level * 0.8);
			if(scroll_level <= 2) y = 0;
		} else if(index + 1 >= data.size() && y > 0)
			scroll_level = -(int)Math.abs(scroll_level * 0.8) - 2;

		synchronized(data) {
		//*** Phase2 要素のスキップ
		int i;
		//Tuwi.log("y: "+y+" index.height:"+i);
		// もう描画の必要のない上側の要素を飛ばす
		while(y > (i = ((Element)data.elementAt(index)).height)
				&& index + 1 < data.size()) {
			if(i == 0) break; // サイズ未計算のおそれあれ
			//Tuwi.log("next");
			y -= i;
			index++;
		}
		// うええ
		while(y < 0 && index > 0) {
			//Tuwi.log("back");
			if((i = ((Element)data.elementAt(index - 1)).height) == 0)
				break;
			index--;
			y += i;
		}

		//*** Phase3 リンクの移動
		/**
		 * link at +1
		 *  範囲内?
		 *   リンク?
		 *    次へ
		 *    :最初へ
		 *   :次のdataある?
		 *    次のdataへ
		 *    :なにもしない;
		 *  次＝
		 *   画面特定領域内?
		 *    フォーカス移動
		 *    :なにもしない;
		 */
		int l = link_at;
		i = focus_at;
		Element e = (Element)data.elementAt(i);
		int j = e.getLinkYpos(l);
		// 行き過ぎた戻せ
		/*if(i < index || last <= j) {
			Tuwi.log("戻した");
			i = index;
			l = 0;
		}
		//*/
		while(true) {
			e = (Element)data.elementAt(i);
			l -= dir;
			//Tuwi.log("l: "+l);
			if(0 <= l && l <= e.format.length) {
				if(e.getLinkType(l) > 0) {
					j = e.getLinkYpos(l);
					//Tuwi.log("j: "+j);
					if(0 <= j && j + 100 < mc.getHeight()) {
						link_at = l;
						focus_at = i;
					}
					break;
				} else {
					continue;
				}
			} else {
				i -= dir;
				//Tuwi.log("i: "+i);
				if(0 <= i && i < data.size()) {
					if(dir < 0) {
						Tuwi.log("l: -1 <= "+l);
						l = -1;
					} else {
						Tuwi.log("lformat.length: " + l);
						l = ((Element)data.elementAt(i)).format.length + 1/*name分*/;
					}
					continue;
				} else
					break;
			}
		}
		}
		//Tuwi.log("i:"+focus_at+ ", l:"+link_at);
		mc.repaint();
		} catch(Exception e) {
			Tuwi.log(e);
		}
	}

	// from http://lecture.ecc.u-tokyo.ac.jp/~cichiji/cp-03/cp-03-12-2.html
	public void sort(Vector a, int left, int right) {			// クイックソート(昇順)
		if (left < right) {
			int somewhere = (left + right) / 2;		// 対象の中央にあるものを基準値として選定
			Element pivot = (Element)a.elementAt(somewhere);			// 基準値の設定
			a.setElementAt(a.elementAt(left), somewhere);
			//a[somewhere] = a[left];			// 基準値を選んだ場所に一番左の要素を入れる
			int p = left;
			for (int i = left + 1; i <= right; i++) {
				Element ai = (Element)a.elementAt(i);
				if (ai.created_at/*a[i]*/ < pivot.created_at) {			// a[i]が基準値より小さいければ，
					p = p + 1;				// 値を入れる配列インデックスpを計算し，
					swap(a, p, i);				// a[p]とa[i]を交換する．
				}					// a[left+1]からa[p]までは基準値より小さい．
				//else if(ai.created_at == pivot.created_at)
				//	a.removeElementAt(i);	// 重複削除
			}
			a.setElementAt(a.elementAt(p), left);
			//a[left] = a[p];				// a[left]にa[p]を代入
			a.setElementAt(pivot, p);
			//a[p] = pivot;				// 基準値をa[p]に入れる．
			/* これでa[left]からa[p-1]はa[p]未満，a[p+1]からa[right]はa[p]以上となる．*/

			sort(a, left, p-1);				// 分割したものにクイックソートを適用
			sort(a, p+1, right);				// 分割したものにクイックソートを適用
		}
	}

	protected void swap(Vector a, int i, int j) {				// 要素の交換
		Object tmp = a.elementAt(i);
		a.setElementAt(a.elementAt(j), i);
		a.setElementAt(tmp, j);
		// int tmp = a[i];
		// a[i] = a[j];
		// a[j] = tmp;
	}
}