package msgpack;

import java.util.Hashtable;

public class ExHash extends Hashtable {
	// null, false, 0, "" ‚Ì‚Æ‚«false‚ð•Ô‚·
	public boolean bool(Object key) {
		key = get(key);
		return !(key == null || key.equals(Boolean.FALSE) || key.hashCode() == 0);
	}
	
	public long Long(Object key) {
		key = get(key);
		if(key == null)
			return 0;
		if(key instanceof Long)
			return ((Long)key).longValue();
		return key.hashCode();
	}
	
	public void incr(Object key, long n) {
		put(key, new Long(Long(key) + n));
	}
	
	public String str(Object key) {
		key = get(key);
		if(key == null)
			return "";
		return key.toString();
	}
	
	public Object get(Object key, Object def) {
		key = get(key);
		if(key != null)
			return key;
		return def;
	}
}
