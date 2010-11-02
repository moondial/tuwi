package models;
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
			sender = User.parse((Object[])_[i]);
			usercache[parsing_pos] = sender;
		}
		++parsing_pos; ++i;
		
		// ユーザ情報（重複省略済）を展開
		if(_[i] instanceof Long)
			recipient = usercache[(int)((Long)_[i]).longValue()];
		else {
			recipient = User.parse((Object[])_[i]);
			usercache[parsing_pos] = recipient;
		}
		++parsing_pos; ++i;
		
		setText((String)_[i]);
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
	// TODO:描画, getlinkypos
	public String toString() {
		return created_at+sender.screen_name + "(" + getDate() + "): " + text;
	}
}
