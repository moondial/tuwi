import java.util.Vector;

/**
 * つぶやきのモデルと描画担当のクラス
 **/
public class Status extends Element {
	long reqly2id, replyuid;
	int flags;
	String text, source;
	User user;
	
	/**
	Status[-sid, -created_at, flags[truncated,faved,following,protected,verified], flags[hassource,hasreply(,hasgeo)],
	<user>, text, [source, repid, repuid, repname(,geo,coordinates,place,contributors)]]
	<user> = number or User()
	Retweet[-sid, -created_at, flags[truncated,faved,following,protected,verified], flags[hassource,hasreply(,hasgeo)],
	<user>, <RT status>, [source, repid, repuid, repname(,geo,coordinates,place,contributors)]]
	*/
	
	// RTしたユーザを含めた、ユーザ情報出現回数。省パケ用。
	public static User[] usercache;
	public static int parsing_pos;
	
	protected Status(Object[] _) {
		super(_);
		int i = 0;
		
		id = ((Long)_[i++]).longValue();
		created_at = ((Long)_[i++]).longValue();
		flags = (int)((Long)_[i++]).longValue();
		int flags2 = (int)((Long)_[i++]).longValue();
		
		//Tuwi.log(_[i]);
		// ユーザ情報（重複省略済）を展開
		if(_[i] instanceof Long)
			user = usercache[(int)((Long)_[i]).longValue()]; // parsing_posと一緒になるよう鯖調整されている
		else {
			user = new User((Object[])_[i]);
			if(usercache != null)
				usercache[parsing_pos] = user;
		}
		++parsing_pos; ++i;
		//Tuwi.log(parsing_pos+","+_[i-1]+","+user);
		//Tuwi.log("text="+_[i]+user);
		// RTの場合は無視する
		if(_[i] instanceof String) {
			text = (String)_[i];
			init(text);
			text_chunk[0] = user.screen_name;
		}
		++i;
		//Tuwi.log("flg");
		// has source
		if((flags2 & 0x1) != 0) source = (String)_[i++];
		// has reply ステータスIDの方は空になることがある
		if((flags2 & 0x2) != 0) {
			if(_[i++] != null)
				reqly2id = ((Long)_[i - 1]).longValue();
			replyuid = ((Long)_[i++]).longValue();
			// ↓使いどころないので削る
			//reqlyname = (String)_[i++];
		}
		// geoなどは実装なし
		//Tuwi.log("/status");
	}
	
	public static Status parse(Object[] _) {
		// if RT?
		if(_[5] instanceof Object[])
			return new Retweet(_);
		else
			return new Status(_);
	}
	
	public static void unpack(Object[] _, Vector data) {
		usercache = new User[1000];
		parsing_pos = 0;
		long latest_id = 0, latest_date = 0;
		for(int i=0; i<_.length; ++i) {
			Status s = parse((Object[])_[i]);
			latest_id -= s.id;
			s.id = latest_id;
			latest_date -= s.created_at;
			s.created_at = latest_date;
			data.addElement(s);
		}
		usercache = null;
	}
	
	public boolean isTruned() {
		return (flags & 1) != 0;
	}
	
	public boolean isFaved() {
		return (flags & 2) != 0;
	}
	
	public boolean isFollowing() {
		return (flags & 4) != 0;
	}
	
	public boolean isProtected() {
		return (flags & 8) != 0;
	}
	
	public boolean isVerified() {
		return (flags & 16) != 0;
	}
	
	// after favorited, etc
	public void overwrite(Status s) {
		flags = s.flags;
		user = s.user;
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
		return user.getName() + '(' + getAbsDate() + "): " + text + '\n';
	}
}
