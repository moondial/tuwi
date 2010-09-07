import java.util.Hashtable;
import java.util.Vector;

import msgpack.ExHash;

public class Account extends Element{
	private static Vector accounts;
	
	// ぬるぽ対策 = "";
	public User user;
	public String password = "";
	public String oauth_token = "";
	public String oauth_token_secret = "";
	public String token = "";
	public String shortname = "";
	
	public int token_expire = 0;
	public long read_id = 0;
	public int get_count = 200;  // 受信件数
	public long[] api = new long[] {0,0,0,0};  // API[残り, 全体, リセット時間, API実行時間]
	
	Account(ExHash h){
		super(null);
		if (h == null)
			return;
		try {
			user = new User((Object[])h.get("user", new Object[] {new Long(0), ""}));
			shortname = h.str("sname");
			password = h.str("password");
			oauth_token = h.str("oauth_token");
			oauth_token_secret = h.str("oauth_token_secret");
			token = h.str("token");
			token_expire = (int)h.Long("token_expire");
			read_id = h.Long("既読");
			get_count = (int)h.Long("受信件数");
		} catch (Exception e) {}
	}
	
	public Hashtable pack(){
		Hashtable h = new Hashtable(8);
		h.put("user", user.src);
		h.put("sname", shortname);
		h.put("password", password);
		h.put("oauth_token", oauth_token);
		h.put("oauth_token_secret", oauth_token_secret);
		h.put("token", token);
		h.put("token_expire", new Integer(token_expire));
		h.put("既読", new Long(read_id));
		h.put("受信件数", new Long(get_count));
		return h;
	}
	
	public static void load(Object[] o) {
		//if(Tuwi.DEBUG)パスワード認証用
		//	if(o == null || o.length == 0) o = new Object[1];
		accounts = new Vector();
		try {
			for(int i=0; i<o.length; ++i)
				accounts.addElement(new Account((ExHash)o[i]));
		} catch(Exception e) {}
	}
	
	public static Object[] packall() {
		Object[] o = new Object[accounts.size()];
		for(int i=0; i<accounts.size(); ++i)
			o[i] = at(i).pack();
		return o;
	}
	
	public String userid() {
		return user.screen_name;
	}
	
	public String toString() {
		return pack().toString();
	}
	
	public static Account at(int i) {
		try {
			return (Account)accounts.elementAt(i);
		} catch (Exception e) {
			Tuwi.log("アカウント指定失敗 i=" + i);
			if(Tuwi.DEBUG) return at(0);
			return null;
		}
	}
	
	// 先頭に移動
	public void to1st() {
		accounts.removeElement(this);
		accounts.insertElementAt(this, 0);
	}
	
	public static int size() {
		if(Tuwi.DEBUG) return 20;
		return accounts.size();
	}
	
	public static Vector getAccounts() {
		return accounts;
	}
	
	public void setMeta(ExHash m) {
		Object[] a = (Object[])(m.get("api", api));
		api[0] = ((Long)a[0]).longValue();
		api[1] = ((Long)a[1]).longValue();
		api[2] = ((Long)a[2]).longValue();
		api[3] = ((Long)a[3]).longValue();
	}
	
}
