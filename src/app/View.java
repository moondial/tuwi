package app;
import models.Account;

import org.apache.regexp.RE;


public abstract class View {
	public static final int[] emptyEvent = {
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,// 0x0n = 0 1 2 3 4 5 6 7 8 9 * # ----
		0,0,0,0,0,0,0,0,0/*���g�p*/// 0x1n(basic) = �����������@�A- ��- - - ----
		};
	
	public static int[] cpyarr(int[] o) {
		int[] n = new int[o.length];
		for(int i=0; i<o.length; ++i)
			n[i] = o[i];
		return n;
	}
	
	/*public static Object[] join(Object[] o, Object[] oo) {
	    Object[] ooo = new Object[o.length + oo.length];
	    System.arraycopy(o, 0, ooo, 0, o.length);
	    System.arraycopy(oo, 0, ooo, o.length, oo.length);
	    return ooo;
	}*/
	
	// urlize�ɂ��� - �������[��
	// �Ehttp://, https://�ł͂��܂�Ȃ烊���N
	// �E���p@�Ŏn�܂�Ȃ�Ȃ烆�[�U��
	// �E���p#�Ŏn�܂�Ȃ�n�b�V���^�O
	// �E�n�b�V���^�O(15���܂�)�ƃ����N�͑S�p�����e
	public static RE urlize = new RE("(@[\\w_]*|#[\\-\\w\\d_�V-\\u9fa5\\uf900-\\ufa6a]{1,15}|https?://[\\w;/?:@&=+$-_.!~*()#�V-\\u9fa5\\uf900-\\ufa6a]+)");
    public static boolean calculating;
	static int lineHei, lineHeiS, lineHeiL; // line height (midium, small, large)
	String title = "";
	int titlecolor, titledeco;
	MainCanvas mc;
	int //y, height, fAscent,//������Ă�
	_h, _x, _y, xx = 0/*���s���ɂ��ǂ�ʒu*/;
	//Font font;
	Account account;
	
	public int[] onKeyPress = cpyarr(emptyEvent),
               onKeyRepeat = cpyarr(emptyEvent),
               onKeyRelease = cpyarr(emptyEvent);
	
	View() {
		mc = MainCanvas.self;
		//font(0, (int)Tuwi.conf.Long("�����T�C�Y"));
	}
	
	public abstract void render(Box b);
	
	/**
	 * �ȉ��\��ς݃C�x���g�ԍ�(ev)�B
	 *  0 ��C�x���g
	 *  1 View�I����
	 *  2 View�ؑ֎�
	 *  3 �^�C�}�[�C�x���g
	 *  4-15 �Ƃ肠�����\��i�g�r����j
	 *  
	 * true��Ԃ����ꍇ�㑱��View�ɂ��L�[�C�x���g��n��
	 * Popup�Ȃǂ�false��Ԃ���TabView�ɃL�[�C�x���g��n���Ȃ�
	 */
	public abstract boolean handleEvent(int ev, Object o);
	public boolean handleEvent(int ev) {
		return handleEvent(ev, null);
	}
	/*
	class Event {
		Object target;
		int ev;
		Vector boxes;
		Event(Object o, int i, Vector b) {
			target = o;
			ev = i;
			boxes = b;
		}
		void call() {
			handleEvent(ev, target);
		}
		void calliftouched(int x, int y) {
			for(int i=0; i<boxes.size(); ++i)
				if(((Box)boxes.elementAt(i)).hit(x, y))
					call();
		}
	}
	
	Vector events = new Vector();
	
	public void drawlink(Box b, int ev, Object target, String linkstr, boolean selected) {
		Vector v = new Vector();
		if (selected)
			b.str(0x8888ff, linkstr, 0x7ff5fffa, Box.UNDERLINE);
		else
			b.str(0x6666ff, linkstr, 0, 0);
		//if()
			events.addElement(new Event(target, ev, v));
	}
	* /
	
	/**
	 * @param len �S�̗v�f���i�y�[�W�̒����j
	 * @param begin �ŏ��̗v�f�ԍ��i��ʏ�y�ʒu�j
	 * @param end �Ō�̗v�f�ԍ��i��ʉ�y�ʒu�j
	 */
	public void drawScrollBar(int len, int begin, int end) {
		if(begin > end) {
			Tuwi.log("scroll bar: "+begin+","+end);
			return;
		}
		new Box(mc.getWidth() - 8,
				mc.getHeight() * begin / len,
				8,
				mc.getHeight() * (end - begin) / len + 1)
		.rgba(0xaaccccff).fill();
	}
	
}
