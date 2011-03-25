package models;


import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Vector;

import msgpack.Msgpack;

import app.Popup;
import app.Tuwi;
import app.URLUtils;

import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;

public class User extends Element {

	long imgid;
	public String screen_name, name, location;
	//[has[isfull,uid,name,imgid,loc], screen_name, uid, name, imgid, location,
	//	offset, timezone, lang, url, bio, tweets, friends, followers, favorites,
	//	created_at, flags[following,verified,protected,geo_enabled,contributors]]

	// �A�C�R���̎���(MediaImage�ꃖ��)
	WeakReference icon;
	boolean hasIcon;

	private static Hashtable icon_cache = new Hashtable(); // �񎟃A�C�R���L���b�V��
	private static Hashtable icons_bin = new Hashtable(); // �ꎟ�A�C�R���L���b�V��
	private static int �A�C�R������DL�ő吔 = 200;  // ���₵������ƃh�R����300KB�����Ɉ���������
	// �A�C�R���̕��σT�C�Y��1.3KB(�ő刳�k��0.9KB���炢)�Ȃ̂ŏ����]�T������
	private static MediaImage none_icon = MediaManager.getImage("resource:///nobody_icon.gif");

	static {
		try {
			none_icon.use();
		} catch (Exception e) {
			// �I��
			Tuwi.getCurrentApp().terminate();
		}
	}

	// �Ԃ₫���A�C�R��MediaImage�擾
	public static void DLIcons(Vector elements) {
		String q = "";
		Long r;
		Vector v = new Vector();
		for(int i=0; i<elements.size(); ++i) {
			Element s = (Element)elements.elementAt(i);
			if(s instanceof Status) {
				r = new Long(((Status)s).user.imgid);
				if(!v.contains(r) && !icons_bin.containsKey(r)) {
					q = q.concat(",").concat(String.valueOf(s.id));
					v.addElement(r);
				}
			}
			if(s instanceof Retweet) {
				r = new Long(((Retweet)s).RTed_status.user.imgid);
				if(!v.contains(r) && !icons_bin.containsKey(r)) {
					q = q.concat(",").concat(String.valueOf(((Retweet)s).RTed_status.id));
					v.addElement(r);
				}
			}
			else if(s instanceof DirectMessage) {
				r = new Long(((DirectMessage)s).sender.imgid);
				if(!v.contains(r) && !icons_bin.containsKey(r)) {
					q = q.concat(",u").concat(String.valueOf(((DirectMessage)s).sender.id));
					v.addElement(r);
				}
				r = new Long(((DirectMessage)s).recipient.imgid);
				if(!v.contains(r) && !icons_bin.containsKey(r)) {
					q = q.concat(",u").concat(String.valueOf(((DirectMessage)s).recipient.id));
					v.addElement(r);
				}
			}
			if(v.size() >= �A�C�R������DL�ő吔)
				break;
		}
		Tuwi.log(v);
		if(q.equals("")) return;
		try {
			URLUtils u = URLUtils.Request("tuwi/icons", "i=".concat(q.substring(1)), URLUtils.ZIPPED | URLUtils.PACKED);
			Hashtable h = (Hashtable)((Msgpack)u.msg).getObject();
			for(int i=0; i<v.size(); ++i) {
				r = (Long)v.elementAt(i);
				byte[] b = (byte[])h.get(r);
				if(b == null) Tuwi.log("nullicon");
				else icons_bin.put(r, b);
			}
		} catch (Exception e) {
			new Popup(e);
		}
		Tuwi.log(icons_bin.size() + "�̃A�C�R����ێ�");
	}

	public static Hashtable getIMGCache() {
		return icons_bin;
	}

	public static void setIMGCache(Hashtable h) {
		if(h != null) icons_bin = h;
	}

	public static User parse(Object[] _) {
		// is full?
		if((((Long)_[0]).longValue() & 1) == 1) {
			return new UserDetail(_);
		} else {
			return new User(_);
		}
	}

	protected User(Object[] _) {
		super(_);
		int i = 0;
		long flg = ((Long)_[i++]).longValue();
		boolean full = (flg & 1) == 1;
		screen_name = (String)_[i++];
		if(full || ((flg & 2) != 0))
			id = ((Long)_[i++]).longValue();
		if(full || ((flg & 4) != 0))
			name = (String)_[i++];
		if(full || ((flg & 8) != 0))
			imgid = ((Long)_[i++]).longValue();
		if(full || ((flg & 16) != 0))
			location = (String)_[i++];
	}

	public MediaImage getIcon() {
		// icon_cache->raw cache->noneicon
		if(hasIcon) {
			if(icon != null && icon.get() != null)
				return (MediaImage)icon.get();
			else
				hasIcon = false;
		}
		icon = (WeakReference)icon_cache.get(new Long(imgid));
		if(icon == null || icon.get() == null) {
			byte[] i = (byte[])icons_bin.get(new Long(imgid));
			if(i == null)
				return none_icon;
			icon = new WeakReference(MediaManager.getImage(i));
			try { // �������C���[�W���e�X�g
				((MediaImage)icon.get()).use();
				((MediaImage)icon.get()).getImage();
			} catch (Exception e) {
				icons_bin.remove(new Long(imgid));
				return none_icon;
			}
			icon_cache.put(new Long(imgid), icon);
		}
		hasIcon = true;
		return (MediaImage)icon.get();
	}

	public String getName() {
		long i = Tuwi.conf.Long("���O�\��");
		if(i == 0) return "��";
		if(name == null) return screen_name;
		if(i == 1) return screen_name;
		if(i == 2) return name;
		return screen_name + "/" + name;
	}

	public String toString() {
		return screen_name;
	}
}
