package app;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.microedition.io.Connector;

import models.Account;
import models.User;
import msgpack.Msgpack;
import msgpack.ExHash;


import com.nttdocomo.net.URLEncoder;
import com.nttdocomo.ui.*;

public class Tuwi extends IApplication{
	MainCanvas c;
	public static String SP_URI = "scratchpad:///0;pos=0"; // scratchpad uri
	public static String BASE = "http://moondial0.net/";
	public static String version = "0.32";
	public static boolean DEBUG = false;
	public static ExHash conf; // ���ʐݒ�
	//public static String[] msg_log = new String[1000];
	//public static int msg_cnt;
	public static Tuwi self; // ���g�ւ̌��J�Q��

	public void start() {
		self = this;
		// �f�o�b�O���p
		if (!getSourceURL().startsWith("http://moondial0.net/")) {
			BASE = "http://self.moondial0.net:8000/";
			DEBUG = true;
		}
		// �`��J�n�O�ɐݒ�ǂݍ��݁i�ʂ�ۉ���j
		loadConf();

		Account.load((Object[])conf.get("accounts"));
		log(Account.getAccounts().toString() + conf);

		// oAuth �F�؊����y�[�W����N��
		if (getLaunchType() == IApplication.LAUNCHED_FROM_BROWSER &&
				getParameter("mode") != null &&
				getParameter("mode").equals("auth")) {
			Account a = new Account(null);
			a.user = User.parse(new Object[] {
					new Long(2), getParameter("user"), new Long(Long.parseLong(getParameter("userid")))});
			//a.user.id = Integer.valueOf(getParameter("userid")).intValue();
			//a.user.screen_name = getParameter("user");
			a.oauth_token = getParameter("token");
			a.oauth_token_secret = getParameter("token_secret");
			Account.getAccounts().addElement(a);
			saveConf();
			dialog(0, "�F�؊���", "�A�J�E���g��ǉ��ł��܂����B");
		}
		//for(int i=0;i<1000;++i)log(i+"");
		// create account for debugging
		/*
		Account a = new Account(null);
		a.user = new User(new Object[] {new Long(2), "moondial0", new Long(Long.parseLong("0"))});
		a.oauth_token = "38650958-YDeabZxA5nopJy7xb0bY5MzFWKqHTaczgzgWiM58";
		a.oauth_token_secret = "wRfHI6bcZzbv4SgN3nNrUs0DNzZLkP6dYSQmJv4";
		Account.getAccounts().addElement(a);
		Tuwi.log(Runtime.getRuntime().freeMemory()+"");
		//*/
//*
		//if(true) {}
//*/
		//else{}
//*/
		// �`��̈搶��
		// ���݂̉�ʂɐݒ�
		Display.setCurrent(new MainCanvas(this));
		/*RichText r =new RichText();
		r.setText("@a#s@");
		r.getSplitedText();*/
	}

	public static Object log(Object o) {
		// TODO:regexp �J��
		LogView.log(o.toString());
		return o;
	}

	public static void loadConf() {
		Msgpack m = null;
		try {
			m = new Msgpack(Connector.openInputStream(SP_URI));
			if (((Long) m.getObject()).longValue() == 3) {
				conf = (ExHash)m.getObject();
				User.setIMGCache((Hashtable)m.getObject());
			}
			m.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {m.close();} catch (Exception e) {}
			if(conf == null)
				conf = new ExHash();
		}
	}

	public static void saveConf() {
		Msgpack m = null;
		try {
			m = new Msgpack(Connector.openOutputStream(SP_URI));
			// ����N�����ʗp
			m.add(3);
			conf.put("accounts", Account.packall());
			//conf.put("tabs", packtabs());
			// �ݒ�ۑ�
			m.add(conf);
			m.add(User.getIMGCache());
			// �O�̂��߃X�g�b�p�[
			m.add((Object)null);
			m.flush();
			Tuwi.log("saved.");
		} catch (IOException e) {
			Tuwi.log(e + " SP�������ς��ł��B");
			//e.printStackTrace();
		} finally {
			try {m.close();} catch (Exception e) {}
		}
	}

	/*public static Object packtabs() {
		Vector tabs = TabView.tabList;
		Vector out = new Vector();
		Vector accounts = Account.getAccounts();
		for(int i=0; i<tabs.size(); ++i) {
			Object tab = tabs.elementAt(i);
			if(tab instanceof TimelineView) {
				TimelineView o = (TimelineView)tab;
				out.addElement(new Object[] {
						new Long(accounts.indexOf(o.account)),
						o.name,
						o.url,
						o.});
			}
		}
		return null;
	}*/
	/*public static Object getConf(Object key, Object def) {
		if ((key = conf.get(key)) != null)
			return key;
		return def;
	}

	public static long getNum(Object o) {
		Object i = conf.get(o);
		if(i == null) return 0;
		if (i instanceof Long)
			return ((Long) i).longValue();
		if (i instanceof Integer)
			return ((Integer) i).longValue();
		return 0;
	}

	public static void addNum(Object o, long i) {
		conf.put(o.toString(), new Long(getNum(o) + i));
	}*/

	public static int dialog(int type, String title, Object msg) {
		Dialog d = new Dialog(type, title);
		d.setText(msg.toString());
		d.setFont(Font.getDefaultFont());
		return d.show();
	}

	public void openURI(String url) {
		openURI(url, "http://www.google.com/gwt/x?u=");
	}
	public void openURI(String url, String cushionLink) {
		try {
			// TODO:���e�X�g
			if(cushionLink == null)
				url = new String(url.getBytes("UTF-8"));
			else
				url = cushionLink + URLEncoder.encode(new String(url.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			log("openbrowser url: " + url);
			// ���C�����C���������ق����T�C�Y�I�ɗL���B20�o�C�g���팸�ł���B
			//String s[] = new String[] { url };
			try {
				launch(IApplication.LAUNCH_BROWSER_SUSPEND, new String[] { url });
			} catch (Exception e) { // �T�X�y���h��Ή�
				launch(IApplication.LAUNCH_BROWSER, new String[] { url });
			}
			// TODO:ibis openAPI�Ή�
		} catch (Exception e) { // ���Ƃ����Â�
			log("openbrowser Error: " + e.toString());
		}
	}
	public static void openTab(View o) {
		MainCanvas.rootView.handleEvent(4, o);
	}
	public static void closeTab(View o) {
		MainCanvas.rootView.handleEvent(5, o);
	}
}

