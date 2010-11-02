package app;


import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;

/**
 *
 */
public class AppConfig extends View {
	// static()���\�b�h�̐�����h�����߂����ɂ͏����Ȃ�
	private static Input[] SETS;
	private static MediaImage menuimg;
	private static int index;
	private static int[] f;
	
	public AppConfig() {
		super();
		title = "�A�v���ݒ���";
		String[] s;
		try {
			//name=0/0+"";
			f = Font.getSupportedFontSizes();
			index = f.length;
			//if(!Tuwi.DEBUG)
				for(index=0; f[index]<=mc.getWidth()/8 &&index<f.length; ++index);
			s = new String[index];
			for(int i=0; i<index; ++i)
				s[i] = f[i] + "pt";
			index = 0;
		} catch (Exception e) {
			s = new String[] {"��", "��", "��", "��"};
		}
		SETS = new Input[] {
				new Input(title),
				new Input("fontSize", "�����̑傫��", s),
				new Input("showIcons", "�A�C�R���\��", true),
				new Input("autoIconDL", "�A�C�R�������擾", true),
				new Input("���t����", "���t����", new String[] {"��ɐ�Ύ���", "��ɐ�Ύ���(+�b)", "�o�ߎ���", "�o�ߎ���(+�b)", "�Ȃ�", "�Ȃ�(+�b)"}),
				new Input("���O�\��", "���O�\��", new String[] {"�Ȃ�", "�A�J�E���g��", "���O", "�A�J�E���g/���O"}),
				new Input("���ݒn�\��", "���ݒn�\��", true),
				new Input("�N���C�A���g�\��", "�N���C�A���g�\��", true),
				new Input("��M����", "��M����", 50, 200),
				//new Input("���y����", "���y����", 50, 200),
				/*new Input(),new Input("test"),
				new Input("��", "��", "defo", 4),*/
				new Input("�ۑ�����Ǝ���N���������̐ݒ���g���܂��B"),
				new Input("�ۑ�", 19),
				new Input("����", 20)
		};
		menuimg = MediaManager.getImage("resource:///checkbox_min.gif");
		try {
			menuimg.use();
		} catch (Exception e) {
			Tuwi.log("menuimg�g����");
		}
	}
	
	class Input {
		String name, source;
		//Object def; // default
		int type, length;
		String[] names;
		//Object[] values;
		
		// border line
		Input() {
			type = 0;
		}
		// label
		Input(String s) {
			type = 1;
			name = s;
		}
		// checkbox
		Input(String s, String n, boolean d) {
			type = 10;
			source = s;
			name = n;
			if(!Tuwi.conf.containsKey(s))
				Tuwi.conf.put(s, new Boolean(d));
		}
		// text(String)
		Input(String s, String n, String d, int l) {
			type = 20;
			source = s;
			name = n;
			if(!Tuwi.conf.containsKey(s))
				Tuwi.conf.put(s, d);
			length = l;
		}
		// text(0�ȏ�̐���)
		Input(String s, String n, int d, int max) {
			type = 21;
			source = s;
			name = n;
			if(!Tuwi.conf.containsKey(s))
				Tuwi.conf.put(s, new Long(d));
			length = max;
		}
		// list
		Input(String s, String n, String[] ns/*, Object[] v*/) {
			type = 30;
			source = s;
			name = n;
			names = ns;
			//values = v;
		}
		// button
		Input(String n, int ev) {
			type = 40;
			name = n;
			length = ev;
		}
	}
	
	public void render(Box b) {
		// �]���̐ݒ�
		b.setWidth(5, Box.auto, 5).str(0, "");
		
		int selected = 0xffE8F2FE;
		int raw = (mc.getHeight() - b._y) / (b._h * 2 + 4);
		
		if(raw == 0) ++raw;
		//Tuwi.log("stage1");
		// �ݒ荀��
		int i = index / raw * raw; // �����؂�̂Ă�����
		for(; i < SETS.length; ++i) {
			Box row = b.child().setWidth(0, Box.auto, 0).setHeight(2, b._h*2, 2).rgba2(index == i, 0, selected).fill();
			//Tuwi.log("a"+SETS[i].type);
			switch (SETS[i].type) {
			case 0:  // border
				row.y += b._h/2-1;
				row.setHeight(0, 2, 0).rgb(0).fill();
				break;
			case 1:  // label
				row.str(0, SETS[i].name);
				break;
			case 10: // checkbox
				int j = 0, k = 0;
				if(index == i)
					j = 19;
				if(Tuwi.conf.bool(SETS[i].source))
					k = 19;
				row.img(menuimg, 5, row._y-1, b._h/2+1, b._h/2+1, j, k, j+19, k+19);
				row.str(0, "   "+SETS[i].name);
				break;
			case 20: // text
			case 21:
				row.str(0, SETS[i].name).newLine()
				 .button(0, Tuwi.conf.str(SETS[i].source), 0, 0);
				break;
			/*case 21:
				row.str(0, SETS[i].name).newLine()
				 .button(0, Tuwi.conf.str(SETS[i].source), 0, 0);
				break;*/
			case 30: // list
				//Tuwi.log("list"+SETS[i].name+(int)Tuwi.conf.Long(SETS[i].source));
				//Tuwi.log(SETS[i].names[(int)Tuwi.conf.Long(SETS[i].source)]);
				row.str(0, SETS[i].name).newLine()
				 .button(0, SETS[i].names[(int)Tuwi.conf.Long(SETS[i].source)], 0, 0).str(0, "��");
				break;
			case 40: // button
				row.button(0, SETS[i].name, 0, 0);
				break;
			}
			
			b.newLine();
			if(b._y > mc.getHeight()) break;
		}
		
		drawScrollBar(SETS.length, index / raw * raw, i);
	}

	public boolean handleEvent(int ev, Object o) {
		switch (ev) {
		case 1: // onfocus
			onKeyPress[Display.KEY_UP] = 16;
			onKeyPress[Display.KEY_DOWN] = 17;
			onKeyRepeat[Display.KEY_UP] = 16;
			onKeyRepeat[Display.KEY_DOWN] = 17;
			onKeyRelease[Display.KEY_SELECT] = 18;
			mc.setSoftLabel(Frame.SOFT_KEY_1, null);
			mc.setSoftLabel(Frame.SOFT_KEY_2, null);
			break;
		case 16: // ��
			if(--index < 0) index = 0;
			break;
		case 17: // ��
			if(++index >= SETS.length) --index;
			break;
		case 18: // �m��
			if(index < SETS.length) {
				Input in = SETS[index];
				switch (in.type) {
				case 10: // checkbox
					Tuwi.conf.put(in.source, new Boolean(!Tuwi.conf.bool(in.source)));
					break;
				case 20: // text
					if(in.length == 0)
						in.length = com.nttdocomo.ui.TextBox.INPUTSIZE_UNLIMITED;
					mc.imeInput(this, 30, 0, Tuwi.conf.str(in.source), com.nttdocomo.ui.TextBox.KANA, in.length);
					break;
				case 21: // text integer
					mc.imeInput(this, 31, 0, Tuwi.conf.str(in.source), com.nttdocomo.ui.TextBox.NUMBER, 3);
					break;
				case 30: // list
					if((ev = (int)Tuwi.conf.Long(in.source) + 1) >= in.names.length)
						ev = 0;
					Tuwi.conf.put(in.source, new Long(ev));
					break;
				case 40: // button
					handleEvent(in.length);
					break;
				}
			}
			mc.setFonts();
			break;
		case 19: // �ۑ��E����
			Tuwi.saveConf();
			new Popup("�A�v���ݒ��ۑ����܂����B", 3000);
		case 20:
			Tuwi.closeTab(this);
			break;
		case 30:
			Tuwi.conf.put(SETS[index].source, o);
			break;
		case 31:
			try {
				ev = (int)Long.parseLong((String)o);
				if(0 <= ev && ev <= SETS[index].length)
					Tuwi.conf.put(SETS[index].source, new Long(ev));
				else
					new Popup("0����"+SETS[index].length+"�͈̔͂ł��肢���܂�", 5000);
			} catch (Exception e) {
				new Popup("0�ȏ�̐�������͂��Ă�������", 5000);
			}
			break;
		default:
			break;
		}
		mc.repaint();
		return false;
	}
	
	public String toString() {
		return "AppConfig";
	}
}
