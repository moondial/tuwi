import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;

public class AccountMenu extends View {
	// TODO preset�������ăJ�X�^�}�C�Y
	// preset �v�� �ǂ�ȃ^�u���E�^�u�����E�^�u�̓��e(�A�J�E���g�A�^�u���AURL�A�c�ʒu�A������)
	// View#packunpack(ExHash or null):Object[]������
	static Element[] VIEWS = {
		new Element(101, 0, "home timeline"),
		new Element(102, 0, "mentions"),
		new Element(103, 0, "your tweets"),
		new Element(104, 0, "favorites"),
		new Element(120, 0, "�V�X�e�����O"),
		new Element(105, 0, "list"),
		new Element(107, 0, "search(�{��)"),
		new Element(100, 0, "public timeline"),
		new Element(106, 0, "DM��M��"),
		new Element(109, 0, "����(pcod)"),
		new Element(111, 0, "���{�ꌗTL"),
	};
	
	int m/*��ʏ�� 0:�@�\�I��, 1:�A�J�E���g�I��, 2:�A�J�E���g�ݒ� */,
	f/*�t�H�[�J�X*/,
	fu/*�t�H�[�J�X�������[�U*/,
	fi/*�t�H�[�J�X�����A�C�e��*/,
	index, fh;
	
	AccountMenu(Account a) {
		super();
		account = a;
		title = a.user.screen_name;
		// �A�J�E���g��擪�Ɉړ�
		a.to1st();
		handleEvent(1);
	}
	
	public void render(Box b) {
		// �]���̐ݒ�
		b.setWidth(5, Box.auto, 5);
		// ���O
		b.str(0, account.userid()).newLine();
		
		fh = b._h;
		int selected = 0xffE8F2FE;
		int raw = (mc.getHeight() - b._y) / (fh * 2 + 4) ;
		
		for(int i = index / raw * raw; // �����؂�̂Ă�����
				i < VIEWS.length; ++i) {
			b.child().setHeight(2, fh, 2).rgba2(index == i, 0, selected).fill()
			.str(0, VIEWS[i].text_chunk[0]);
			b.newLine();
			if(b._y > mc.getHeight()) return;
		}
		/*Account a;
		g.setOrigin(0, 0);
		Box b = new Box(0, _y, mc.getWidth(), Box.auto);
		//b.child().setWidth(16, Box.auto, 16).setHeight(10, 100, 10).rgba(0xffffffff).fill();
		//b.str(0, "test����������������������������������������������������������t");
		b.child().str(0,"���{�^��").button(0, "longlonglonglong����a��������������������������", 0, 0).button(0, "test", 0xffffffff, 0)
		.button(0, "longlonglonglong����a��������������������������", 0, 0).button(0, "test", 0xffffffff, 0);
		//drawlink(b, 60, "test", "testlink!", true);
		if(m == 0) {
			g.setOrigin(0, -y);
			xx = 20;
			str(0, "Tuwi v".concat(Tuwi.version)).skipLine();
			str(0xff, account.userid()).skipLine();
			String[] s = {"home timeline", "mentions", "your tweets", "favorites", "�V�X�e�����O","list","public timeline","DM��M��"};
			for(int j=0; j<s.length; ++j) {
				if(f == j)
					str(0, s[j], 0xF3F8FE, 1);
				else
					str(0, s[j]);
				skipLine();
			}
			/*str(f==5?0xFF6347:0, "���[�U��");
			g.drawRect(_x, _y, mc.getWidth() - _x - 5, lineHei);
			_x += 3;
			str(0, account.screen_name).skipLine();
			str(f==6?0xFF6347:0, "�p�X���[�h�ύX").skipLine();
			* /
			str(f==8?0xFF6347:0, "��M����: ".concat(String.valueOf(account.get_count))).skipLine();
			int j = _x;
			_y += 2;
			str(f==9?0xFF6347:0, "�ۑ�");
			g.drawRect(j - 2, _y - 2, _x - j + 4, lineHei + 4);
			skipLine();
			if(f == 10)
				str(0xFF6347, "�A�v���ݒ���", 0xF3F8FE, 1);
			else
				str(0, "�A�v���ݒ���");
			skipLine();
			str(0, "�s��񍐁A���z���� @moondial0 �܂ł��񂹂��������B").skipLine();
			str(0x882222, "�Ԃ₭���́��L�[�I������\n����Đؑւ͉E��̃{�^��");
			str(0, "API "+account.api[0]+"/"+account.api[1]);
		}
		if(m == 1 || m == 2) {
			str(0, "�A�J�E���g�ꗗ�F").skipLine();
			
			int l = Account.size();
			for(int j=0; j<=l; j++) {
				if(fu == j) rgba(0xffE8F2FE);
				else if (j % 2 == 1) rgba(0x0);
				else rgba(0x7fE8F2FE);
				g.fillRect(0, _y, mc.getWidth(), lineHei * 2);
				if(j == l)
					str(0, "���A�J�E���g�ǉ���");
				else {
					a = Account.at(j);
					str(0, a.userid()).skipLine();
					str(0, a.get_count + " " + a.read_id).skipLine();
					if(fu == j && m == 2) {
						a = Account.at(fu);
						str(fi == 0? 0xFF6347:0, "  >>�A�J�E���g�폜��").skipLine();
						str(fi == 1? 0xFF6347:0, "  >>����").skipLine();
					}
				}
			}
		}
		height = _y;*/
	}
	
	public boolean handleEvent(int ev, Object o) {
		//Tuwi.log("TopMenu"+ev);
		switch(ev) {
		case 1: // focus
			if(account == null) {
				handleEvent(30);
				return false;
			}
			// �\�t�g�L�[�̃��x����ݒ�
			mc.setSoftLabel(Frame.SOFT_KEY_1, null);
			mc.setSoftLabel(Frame.SOFT_KEY_2, null);
			m = 0;
			onKeyRelease[Display.KEY_UP] = 16;
			onKeyRelease[Display.KEY_DOWN] = 17;
			onKeyRepeat[Display.KEY_UP] = 16;
			onKeyRepeat[Display.KEY_DOWN] = 17;
			onKeyRelease[Display.KEY_SELECT] = -18;
			onKeyPress[Display.KEY_ASTERISK] = 23;
			onKeyPress[Display.KEY_SOFT1] = 0;
			onKeyPress[Display.KEY_SOFT2] = 30;
			
			onKeyPress[Display.KEY_POUND] = 99;
			break;
		case 2: // unfocus
			break;
		case 16:
			if(--index < 0) index = 0;
			break;
		case 17:
			if(++index >= VIEWS.length) index--;
			break;
		case 18:  // open home timeline
			handleEvent((int)VIEWS[index].id);
			break;
		/*case 19:
			System.out.println("IME Canceled: ".concat(o.toString()));
			break;
		case 20:
			//account.screen_name = (String)o;
			break;
		case 21:
			account.password = (String)o;
			break;
		case 22:
			account.get_count = Integer.parseInt((String)o);
			break;
		
		case 30: // �A�J�E���g�؂�ւ�
			m = 1;
			mc.setSoftLabel(Frame.SOFT_KEY_1, "�ύX");
			onKeyPress[Display.KEY_SOFT1] = 40;
			onKeyPress[Display.KEY_SOFT2] = onKeyPress[Display.KEY_IAPP] = 1;
			onKeyRelease[Display.KEY_UP] = 31;
			onKeyRelease[Display.KEY_DOWN] = 32;
			onKeyRelease[Display.KEY_SELECT] = 33;
			break;
		case 31:
			if(--fu < 0) fu = Account.size();
			break;
		case 32:
			if(++fu > Account.size()) fu = 0;
			break;
		case 33: // �A�N�e�B�u�A�J�E���g�ύX or �쐬
			// �A�J�E���g�쐬
			if(fu == Account.size()) {
				Tuwi.self.openURI(Tuwi.BASE + "static/iappli/oauth.html", null);
			} else {
				Tuwi.conf.put("accountIndex", new Integer(fu));
				account = Account.at(fu);
			}
			handleEvent(1);
			break;
			
		case 40: // �A�J�E���g�ݒ�
			// ��������
			if(fu >= Account.size()) break;
			m = 2;
			mc.setSoftLabel(Frame.SOFT_KEY_1, "���~");
			mc.setSoftLabel(Frame.SOFT_KEY_2, "�߂�");
			onKeyPress[Display.KEY_SOFT1] = onKeyPress[Display.KEY_IAPP] = 30;
			onKeyPress[Display.KEY_SOFT2] = 1;
			onKeyRelease[Display.KEY_UP] = 41;
			onKeyRelease[Display.KEY_DOWN] = 42;
			onKeyRelease[Display.KEY_SELECT] = 43;
			break;
		case 41:
			if(--fi < 0) fi = 1;
			break;
		case 42:
			if(++fi > 1) fi = 0;
			break;
		case 43: // �A�J�E���g�ݒ�őI��
			if(fi == 0) {
				if(Dialog.BUTTON_YES == Tuwi.dialog(
						Dialog.DIALOG_WARNING | Dialog.DIALOG_YESNO, "�x��",
						Account.at(fu).userid() + " ��[������폜���܂��B��낵���ł����H\n(Twitter�A�J�E���g���͍̂폜����܂���)")
				&& Dialog.BUTTON_YES == Tuwi.dialog(
						Dialog.DIALOG_ERROR | Dialog.DIALOG_YESNO, "�{���ɁH�H",
						"�{���� " + Account.at(fu).userid() + " ��[������폜���܂��B\n��@���@���@�܁@���@��@�ˁ@�H")
				){  // ������ĂȂ��炵���̂Ż��ޮ���(�f�X�m�[�g����
					Account.getAccounts().removeElementAt(fu);
					Tuwi.dialog(Dialog.DIALOG_INFO, "�폜���܂���", "���̌�ʓ|�łȂ��̂Ȃ�Twitter�{�Ƃ��獡�̃A�J�E���g��OAuth�ݒ���������Ă��������B\n�Ȃ��A���̃A�J�E���g�ŊJ����Ă����^�u�͏I������܂Ŏc��܂��B");
					Tuwi.saveConf();
				}
			}
			if(fi == 1) {  // ����
				handleEvent(30);
				return false;
			}
			handleEvent(1);
			break;
		case 50:
			
			break;*/
		case 23:
			Tuwi.openTab(new UpdateForm(account));
			break;
		case 100:
			Tuwi.openTab(new TimelineView(account, "���JTL", "publicTL?"));
			break;
		case 101:
			Tuwi.openTab(new TimelineView(account, "�ăz�[��", "h?"));
			break;
		case 102:
			Tuwi.openTab(new TimelineView(account, "�󌾋y", "m?"));
			break;
		case 103:
			Tuwi.openTab(new TimelineView(account, "��"+account.userid(), "U?"));
			break;
		case 104:
			Tuwi.openTab(new TimelineView(account, "��"+account.userid(), "f?"));
			break;
		case 105:
			mc.imeInput(this, 110, 19, null, com.nttdocomo.ui.TextBox.ALPHA, 15);
			break;
		case 110:
			Tuwi.openTab(new TimelineView(account, (String)o, "listTL?slug="+o+"&"));
			break;
		case 106:
			Tuwi.openTab(new TimelineView(account, "�wDM��", "DM?"));
			break;
		case 107:
			mc.imeInput(this, 108, 19, null, com.nttdocomo.ui.TextBox.KANA, 30);
			break;
		case 108:
			Tuwi.openTab(new TimelineView(account, "��"+o, "search?"+URLUtils.urlencode(new String []{"q", (String)o})));
			break;
		case 109:
			mc.imeInput(this, 112, 19, null, com.nttdocomo.ui.TextBox.KANA, 30);
			break;			
		case 112:
			Tuwi.openTab(new TimelineView(account, "��_"+o, "JPSearch?"+URLUtils.urlencode(new String []{"q", (String)o})));
			break;
		case 111:
			Tuwi.openTab(new TimelineView(account, "���{����JTL", "JPPublicTL?"));
			break;
		case 120:
			Tuwi.openTab(new LogView());
			break;
		}
		// �X�N���[���o�[
		//height = (VIEWS.length-1) * (fh * 2 + 4) + mc.getHeight();
		//y = index * (fh * 2 + 4);
		//Tuwi.log(""+ev);
		mc.repaint();
		return true;
	}
	
	public String toString() {
		return "" + account.hashCode();
	}
}
/*class Dummy extends InputStream{
	private int count=0;
	private int[] data = new int[]{
			0x99,
			 0xc0,
			 0xc2,
			 0xc3,
			 0xca, 0x3a, 0x03, 0x12, 0x6f,
			 0xcb, 0x3f, 0x40, 0x62, 0x4d, 0xd2, 0xf1, 0xa9, 0xfc,
			 0xcc, 200,
			 0xcd, 0xff, 0x00,
			 0xce, 0xff, 0xff, 0xff, 0xff,
			 0xcf, 0x3f, 0x40, 0x62, 0x4d, 0xd2, 0xf1, 0xa9, 0xfc,
			 };
	public int read() throws IOException {
		System.out.println(Integer.toHexString((int)(Double.doubleToLongBits(0.0005d)&0xffffffff)));
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return data[count++];
		
	}
	
}*/