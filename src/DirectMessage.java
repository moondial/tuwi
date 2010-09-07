import java.util.Vector;

/**
 * 信符「ダイレクトメッセージ」
 * モデルと描画担当のクラス
 **/
public class DirectMessage extends Element {
	//ユーザ情報。省パケ用。
	public static User[] usercache;
	public static int parsing_pos;
	String text;
	User sender, recipient;
	
	/*Out[10]: DirectMessage(
	 * {u'created_at': 1274922146,
	 * u'sender': User(中略),
	 * u'text': u'（ｒｙ',
	 * u'sender_screen_name': u'jinjitter',
	 * u'sender_id': 115381785,
	 * 'created_at_local': 1274954546,
	 * u'recipient_id': 38650958,
	 * u'recipient_screen_name': u'moondial0',
	 * u'recipient': User(中略),
	 * u'id': 1173140166})
	 必要なのはID,日付,文,送り主と受け手ユーザ */
	
	// DirectMessage[-id, -created_at, sender, recipient, text]
	
	protected DirectMessage(Object[] _) {
		super(_);
		int i = 0;
		
		id = ((Long)_[i++]).longValue();
		created_at = ((Long)_[i++]).longValue();
		
		// ユーザ情報（重複省略済）を展開
		if(_[i] instanceof Long)
			sender = usercache[(int)((Long)_[i]).longValue()];
		else {
			sender = new User((Object[])_[i]);
			usercache[parsing_pos] = sender;
		}
		++parsing_pos; ++i;
		
		// ユーザ情報（重複省略済）を展開
		if(_[i] instanceof Long)
			recipient = usercache[(int)((Long)_[i]).longValue()];
		else {
			recipient = new User((Object[])_[i]);
			usercache[parsing_pos] = recipient;
		}
		++parsing_pos; ++i;
		
		text = (String)_[i];
		init(text);
		text_chunk[0] = sender.screen_name;
		Tuwi.log(text);
	}
	
	public static void unpack(Object[] _, Vector data) {
		usercache = new User[1000];
		parsing_pos = 0;
		long latest_id = 0, latest_date = 0;
		for(int i=0; i<_.length; ++i) {
			DirectMessage s = new DirectMessage((Object[])_[i]);
			latest_id -= s.id;
			s.id = latest_id;
			latest_date -= s.created_at;
			s.created_at = latest_date;
			data.addElement(s);
		}
		usercache = null;
		//Tuwi.log(s[0].text);
	}
	
	/*public int prevFocus(int f) {
		if(f > 0) return --f;
		else return -1;
	}
	public int nextFocus(int f) {
		if(f < text_chunk.length) return ++f;
		else return -1;
	}*/
	public String toString() {
		return created_at+sender.screen_name + "(" + getDate() + "): " + text;
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

		//Tuwi.log("rendering icon");
		if(Tuwi.conf.bool("showIcons"))
			// 文字描画開始位置をアイコン分ずらす
			v.xx = v._x = 48;
		else
			v.xx = v._x = 0;
		// 高さ計算済みならスキップ
		if (v.calculating) {
			v.str(0, sender.screen_name);
		}
		else {
			// showIdons==true ならアイコン描画
			if(v._x != 0)
				v.g.drawImage(sender.getIcon().getImage(), 0, v._y);
			if (focus_at == 0)
				v.str(0xfafafa, sender.screen_name, 0x7f566666, 1);
			else
				v.str(0x566666, sender.screen_name);
		}
		text_ypos[0] = v._y;

		//Tuwi.log("rendering body.");
		v.str(0x566666, ' ' + getDate()).skipLine();
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
		if(sender.location != null)
			v.str(0x776666, '[' + sender.location + ']');
		v.skipLine();
		if (v._y - h < 48)
			v._y += 48 - v._y + h;
		height = v._y - h;
	}*/
}
