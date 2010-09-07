import java.util.Vector;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.TextBox;
import com.nttdocomo.util.Timer;
import com.nttdocomo.util.TimerListener;

public class MainCanvas extends Canvas implements TimerListener {
	public static MainCanvas self;
	public static Timer timer;
	public static TabView rootView;
	public static Vector popups = new Vector();  // �|�b�v�A�b�v�̑҂���
	public static boolean popupPolicy;  // true�Ȃ�rootView��`���ɕ`��, false�Ȃ�popup�̂ݏ㏑���`��
	public static int timer_expire;
	
	public static View ime;
	public static int[] imeev = new int[2];
	
	public static Font[] fonts = new Font[12];
	
	MainCanvas(Tuwi t) {
		self = this;
		timer = new Timer(); // �^�C�}�[������
        timer.setTime(200);
        timer.setRepeat(false);
        timer.setListener(new TimerListener() {
			public void timerExpired(Timer t) {
				if(getKeypadState() == 0 || timer_expire != 0) return;
				t = new Timer();
				t.setTime(60);
				t.setRepeat(true);
				t.setListener(self);
				t.start();
			}
		});
        
        setBackground(Graphics.getColorOfRGB(0xfa, 0xfa, 0xfa));
        setFonts();
        Box.g = getGraphics();
        rootView = new TabView();
	}

	// �`�揇: rootView(TabView).render -> �eView.render -> Popup.render
	// �������� Popup.render�̂�
	public void paint(Graphics g) {
		g.lock();
		Box b = new Box(0, 0, getWidth(), Box.auto).child();
		if(popups.isEmpty() || popupPolicy) {
			g.clearRect(0, 0, getWidth(), getHeight());
			// �^�u�ƒ��g�`��
			rootView.render(b);
		}
		// popup�`��
		if(!popups.isEmpty())
			((View)popups.firstElement()).render(b);
		
		// �����I�ĕ`��
		g.unlock(true);
	}
	
	// �L�[���̓C�x���g�n���h��
	// processEvent -> TabView.key -> �eView.handleEvent
	// �D��x: Popup -> rootView(-> �e�^�u)
	public void processEvent(int type, int param) {
		boolean flg = true;
		if(type < 2) { //if(type == Display.KEY_PRESSED_EVENT || type == Display.KEY_RELEASED_EVENT){
			if(!popups.isEmpty()) {
				View currentView = (View)popups.firstElement();
				if(type == 3/* timer event */) {
					currentView.handleEvent(3, null);
					return;
				}
				int i = 
					type == 0? currentView.onKeyPress[param]:
					type == 1? currentView.onKeyRelease[param]:
					currentView.onKeyRepeat[param];
				// �����Ȃ�X���b�h�N��
				//if(i > 0)
					flg = currentView.handleEvent(i, null);
				//else
				//	URLUtils.createThread(currentView, -i, null);
			}
			if(flg) rootView.key(type, param);
			try {
				timer.start();
			} catch(Exception e) {}
		}
		else Tuwi.log("event: " + type + ", " + param);
	}
	
	/** ���͉�ʂ��J��() */
	public void imeInput(View v, int ev_ok, int ev_ca, String i, int type, int len) {
		ime = v;
		imeev[0] = ev_ok;
		imeev[1] = ev_ca;
		// �L�[���J�������܂ł܂��Ȃ����ime�I�����getKeypadState()��
		// ����L�[�Ȃǂ������ꂽ�܂܂ɂȂ��Ă��܂��o�O�΍�(������o�b�h�m�E�n�E)
		try {
			while(getKeypadState() != 0)
				Thread.sleep(1);
			imeOn(i, TextBox.DISPLAY_ANY, type, len);
		} catch (Exception e) {  // UnsupportedOperationException
			imeOn(i, TextBox.DISPLAY_ANY, type);
		}
		
	}
	
	// �A�j���[�V�����A�^�C�}�[���䂨��уL�[���s�[�g�S��
	public void timerExpired(Timer t) {
		// timer event for animation
		rootView.key(3, 0);
		int k = getKeypadState();
		if(k == 0) {
			//timer_expire -= timer.getResolution();
			if(timer_expire-- < 0) {
				t.stop();
				timer_expire = 0;
			}
			return;
		}
		if(!popups.isEmpty()) {
			View currentView = (View)popups.firstElement();
			for(int i=0; i<0x19; ++i)  // 0�`#
				if((k & 1<<i) != 0)
					if(currentView.handleEvent(currentView.onKeyRepeat[i], null))
						rootView.key(2, i);
		}else
		for(int i=0; i<0x19; ++i)  // 0�`#
			if((k & 1<<i) != 0)
				rootView.key(2, i);
		//Tuwi.log("kps: "+Integer.toBinaryString(getKeypadState()));
	}
	
	// IME�̃��[�U�[���삪��������Ƃ���"processIMEEvent"���\�b�h���Ă΂��
	public void processIMEEvent(int type, String txt) {
		if(type == IME_CANCELED)
			ime.handleEvent(imeev[1], txt);
		else
			ime.handleEvent(imeev[0], txt);
	}
	
	// �t�H���g�̐ݒ�
	public void setFonts() {
		int[] size = {Font.SIZE_TINY, Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
		int[] style = {Font.STYLE_PLAIN, Font.STYLE_BOLD, Font.STYLE_ITALIC, Font.STYLE_BOLDITALIC};
		int index;
		// ��ʕ�1/8���傫���t�H���g�͖���
		try {
			//timer_expire = 0/0;
			size = Font.getSupportedFontSizes();
			for(index=size.length-1; size[index] > getWidth()/8; --index)
				size[index] = size[index - 1];
		} catch (Exception e) {}
		
		index = (int)Tuwi.conf.Long("fontSize") - 1;
		for(int i=0; i<3; ++i) {
			if(index + 1 >= size.length)
				index = size.length - 1;
			// ���ׂẴX�^�C���̃t�H���g���擾
			for(int j=0; j<4; ++j) {
				try {
					fonts[4*i+j] = Font.getFont(Font.FACE_PROPORTIONAL | style[j], size[index < 0?0:index]);
				} catch (Exception e) {
					fonts[4*i+j] = Font.getFont(Font.FACE_PROPORTIONAL | size[index < 0?0:index] | style[j]);
				}
			}
			++index;
		}
		Font.setDefaultFont(fonts[4]);
		
		// �t�H���g�T�C�Y�ύX�ʒm
		View.lineHei = fonts[4].getHeight();
		View.lineHeiS = fonts[0].getHeight();
		View.lineHeiL = fonts[8].getHeight();
		index = TabView.tabList.size();
		for(int i=1; i<index; ++i)
			((View)TabView.tabList.elementAt(index-1)).handleEvent(4);
	}
	
}
