
public class Retweet extends Status {
	public Status RTed_status;
	public Retweet(Object[] _) {
		super(_);
		RTed_status = new Status((Object[])_[5]);
		text = "RT @" + RTed_status.user.screen_name + ": " + RTed_status.text;  // for unofficial RT
		text_chunk = RTed_status.text_chunk;
		text_ypos = RTed_status.text_ypos;
	}
	
	public String toString() {
		return user.screen_name + "(" + getAbsDate() + "): RT @" + RTed_status.user.screen_name + RTed_status.text;
	}
	
	//
	// 描画関連
	//
	/*public void render(View v, int focus_at, int style) {
		int h = v._y;
		// 背景色
		if (style == 0)
			v.rgba(0x0);
		else
			v.rgba(0x7fE8F2FE);
		v.g.fillRect(0, h, v.mc.getWidth(), height);

		//Tuwi.log("rendering 公式RT icon");
		if(Tuwi.conf.bool("showIcons"))
			// 文字描画開始位置をアイコン分ずらす
			v.xx = v._x = 48;
		else
			v.xx = v._x = 0;
		// 高さ計算済みならアイコンスキップ
		if (v.calculating) {}
		else {
			// showIdons==true ならアイコン描画
			if(v._x != 0) {
				MediaImage icon = RTed_status.user.getIcon();
				v.g.drawImage(icon.getImage(), 0, v._y);
				v.g.drawImage(user.getIcon().getImage(), 0, v._y + icon.getHeight());
			}
		}
		if (focus_at == 0)
			v.str(0xfafafa, RTed_status.user.screen_name, 0x7f566666, 1);
		else
			v.str(0x566666, RTed_status.user.screen_name);
		v.str(0x566666, "(RT*" + user.screen_name + ")");
		text_ypos[0] = v._y;

		//Tuwi.log("rendering body.");
		v.str(0x566666, ' ' + RTed_status.getDate()).skipLine();
		for (int j = 1; j < text_chunk.length; ++j) {
			char c = text_chunk[j].charAt(0);
			if (c == ' ') {
				v.str(0, text_chunk[j].substring(1));
				continue;
			} else if (c == '@') {
				if (focus_at == j)
					v.str(0xFF6347, text_chunk[j], 0x7fFEEEF1, 1);
				else
					v.str(0xFF6347, text_chunk[j]);
			} else if (c == '#') {
				if (focus_at == j)
					v.str(0x006400, text_chunk[j], 0x7ff0fff0, 1);
				else
					v.str(0x006400, text_chunk[j]);
			} else { // 'h'
				if (focus_at == j)
					v.str(0x8888ff, text_chunk[j], 0x7ff5fffa, 1);
				else
					v.str(0x6666ff, text_chunk[j]);
			}
			
			text_ypos[j] = v._y;
		}

		//Tuwi.log("rendering footer.");
		if(Tuwi.conf.bool("現在地表示")) {
			if(RTed_status.user.location != null)
				v.str(0x776666, '[' + RTed_status.user.location + ']');
			if(user.location != null)
				v.str(0x776666, '[' + user.location + ']');
		}
		if(Tuwi.conf.bool("クライアント表示"))
			v.str(0x776666, '(' + source + ')');
		v.skipLine();
		if (v._y - h < 48)
			v._y += 48 - v._y + h;
		height = v._y - h;
	}*/
}
