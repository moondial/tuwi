package app;


import java.util.Vector;

import models.Account;
import models.DirectMessage;
import models.Element;
import models.Retweet;
import models.RichText;
import models.Status;
import models.User;


import com.nttdocomo.ui.Display;

class TimelineView extends View {
	public static int MAX_SCROLL_SPEED = 500; // �������u
	public String url;
	public Vector data = new Vector(400);
	/**
	 * @param index �`��J�n�v�f == data[index]
	 * @param y index�`��J�n����y�ʒu�i�������𐳂Ɏ��j
	 * @param focus_at �I�𒆂̗v�f == data[focus_at]
	 * @param link_at �I�𒆂̃����N�̔ԍ�
	 * @param scroll_level �K�����ȁi�m
	 *
                 ,�L _,, '-�L�P�P�M-�T �A_ �C�A
                'r �L          �R�A݁A
                ,'��=��-      -��=��', i
                i � i�T�A��l���^_���R� i |
                �زi (�_]     �_� ).| .|�Ai .||
                 !Y!""  ,�Q__,   "" �u !� i |
                 L.',.   �R _�    L�v �| .|
                 | ||�R�A       ,�| ||�| /
                 �� ���M �[--�� �L��� ځL

                  ����������������
                  �� 1��  ��150 ��
                  ����������������
	 */
	public int index, y, focus_at, link_at, scroll_level, last;

	TimelineView(Account a, String n, String u) {
		account = a;
		title = n;
		url = u;
		onKeyPress[3] = 22;
		onKeyPress[4] = 27;
		onKeyPress[9] = 23;
		onKeyPress[Display.KEY_ASTERISK] = 30;
		onKeyPress[Display.KEY_UP] = 20;
		onKeyPress[Display.KEY_DOWN] = 21;
		onKeyRepeat[Display.KEY_UP] = 20;
		onKeyRepeat[Display.KEY_DOWN] = 21;
		onKeyRelease[Display.KEY_SELECT] = -42;
		onKeyRelease[Display.KEY_SOFT1] = -40;
		onKeyRelease[Display.KEY_SOFT2] = -41;
		handleEvent(1);
	}

	public void render(Box b) {
		try {
		b._y -= y;
		//Tuwi.log(y+" "+b._y);
		// �]���̐ݒ�
		b.setWidth(0, Box.auto, 0);
		// ���O
		//b.str(0, account.userid()).newLine();

		//int selected = 0xffE8F2FE;
		//int raw = (mc.getHeight() - b._y) / (b._h * 2 + 4) ;
		//b.str(0, URLUtils.loadedBytes + "bytes �v" + Tuwi.conf.Long("dlBytes") / 1024 + "Kbytes "+data.size()+"�{").newLine();
		if(data.isEmpty()) {
			b.str(0, "[����]�L�[:�Ԃ₫���_�E�����[�h\n[4]�L�[:�A�C�R���擾");
			return;
		}
		//Object focus = ((Element)data.elementAt(index)).links[link_at];
		int iconW = 0;
		if(Tuwi.conf.bool("showIcons"))
			iconW = 48;

		int i;
		for(i=index; i<data.size(); ++i) {
			Element elm;
			synchronized(data) {
				elm = (Element)data.elementAt(i);
			}
			elm.y = b._y;
			if(elm instanceof Status) {
				Status s = (Status)elm;
				/* ��s�\��
				b.child().setHeight(0, lineHei, 0)
				.rgba2(i%2==0, 0x7f7f7fff, 0).fill()
				.img(s.user.getIcon(), lineHei, lineHei)
				.clip()
				.str(0, s.text)
				.parent().newLine();
				//*/
				//* �ʏ�\��
				boolean isRT = elm instanceof Retweet;
				if(isRT)
					s = ((Retweet)s).RTed_status;
				// ������ɂ������
				String footer = "", date = s.getDate(), name = s.user.getName();
				if(Tuwi.conf.bool("���ݒn�\��")) {
					if(s.user.location != null)
						footer = '[' + s.user.location + ']';
					if(isRT && ((Status)elm).user.location != null)
						footer += '[' + ((Status)elm).user.location + ']';
				}
				if(Tuwi.conf.bool("�N���C�A���g�\��"))
					footer += '(' + s.source + ')';
				if(isRT)
					name += "(RT*" + ((Status)elm).user.getName() + ")";

				// ������\�ߌ��߂Ă���
				int height = b.child().setWidth(iconW, Box.auto, 0)
				.measure(name, 0)
				.measure(date, Box.�E���� | Box.NOBREAK).newLine()
				.measure(s.text+footer, 0).boxHeight();
				// �c���͍Œ�ł��A�C�R�������ȏ�
				if(height < iconW) height = iconW;
				((Element)elm).height = height;

				Box bb = b.child()
				.setWidth(0, Box.auto, 0)
				.setHeight(0, height, 0)
				.rgba2(i%2==0, 0x7fE8F2FE, 0).fill()
				.img(s.user.getIcon(), iconW, iconW);
				if(isRT)
					bb.newLine().img(((Status)elm).user.getIcon(), iconW, iconW)._y -= iconW;
				bb.child()
				.setWidth(0, Box.auto, 0)
				.linkstr(name, 0x566666, 0xfafafa, 0x7f566666, i == focus_at && link_at == 0)
				.str(0x566666, date, 0, Box.�E���� | Box.NOBREAK).newLine()
				.richstr(s.getSplitedText(), s.format, i == focus_at ?link_at - 1:-1)
				.str(0x776666, footer);
				b._y += height;
				//*/
				// TODO: Box.if
			}
			// �`���~
			if(b._y > mc.getHeight()) break;
		}
		last = i;

		b.parent(); // unclip
		drawScrollBar(data.size(), index, index + 5);
		// todo: focus, click events
		} catch(Exception e) {
		}
	}

	public boolean handleEvent(int ev, Object o) {
		if(ev > 21 || ev < -21)
			Tuwi.log("TimelineView#" + ev);

		switch (ev) {
		case 1: // focus
			mc.setSoftLabel(0, "����");
			mc.setSoftLabel(1, "�V��");
			break;
		case 2:
			break;
		case 3:
			// �����L�[�������ꂽ�܂�
			if (scroll_level == 0 || mc.getKeypadState() != 0)
				return false;
			++MainCanvas.timer_expire;
			// ������肤�����Ă����ĂˁI
			scroll(0);
			break;
		case 16:
			title = "��" + title;
			Popup p = new Popup("�^�C�����C���Ǎ���...", 0);
			URLUtils res = URLUtils.APIRequest(account, (String) o + "&c="+Tuwi.conf.str("��M����"), null);
			title = title.substring(1);
			p.close();
			if(res.code != 200) {
				if(res.code == 400)
					new Popup("API�����̂悤�ł�\n"+res.msg, 5000);
				if(res.code == 403)
					new Popup("�A�N�Z�X��������܂���\n"+res.msg, 5000);
				if(res.code == 500)
					new Popup("�T�[�o�[�G���[\n"+res.msg, 5000);
				if(res.code == 502)
					new Popup("�T�[�o�[�_�E���������e�i���X��\n"+res.msg, 5000);
				if(res.code == 503)
					new Popup("�T�[�r�X�����s�\", 5000);
				if(res.code == 504)
					new Popup("�ڑ��G���[", 5000);
				if(res.code >= 1000)
					new Popup(res.msg, 5000);
				return false;
			}
			p = new Popup("���ёւ���...", 0);
			try {
				if(res.meta.str("fmt").equals("DM")) {
					DirectMessage.unpack((Object[])res.msg, data);
				} else
					Status.unpack((Object[])res.msg, data);
				Tuwi.log(data.size()+"");
				synchronized(data) {
					sort(data, 0, data.size() - 1);
					// �d���폜
					for(int i=0; i<data.size()-1; ++i)
						if(((Element)data.elementAt(i)).id == ((Element)data.elementAt(i+1)).id)
							data.removeElementAt(i);
				}
				if(url.charAt(0) == 'h')
					account.read_id = Math.max(((Element)data.firstElement()).id, ((Element)data.lastElement()).id);
				Tuwi.saveConf();

				/*o = data[index];  // �t�H�[�J�X��ۑ�
				index = -1;
				quickSort(data, 0, data.length - 1, false);

				for (ev = 0; ev < data.length; ++ev) {
					data[ev].height = 0;
					if(data[ev] == o) index = ev;
				}
				if(index == -1)
					index = link_at = 0;

				first_id = Math.min(data[0].id, data[data.length - 1].id);
				latest_id = Math.max(data[0].id, data[data.length - 1].id);

				// TODO: if home>�C�ӎw���
				if(url.charAt(0) == 'h') account.read_id = latest_id;
				Tuwi.saveConf();

				//* / �����I�Ɉ�x�`�悳����
				Tuwi.log("y: "+y+" "+data[index].chunk_ypos[link_at]+" "+index+" "+link_at);
				y = 0x40000000;
				_y = 0;
				onKeyRepeat[Display.KEY_UP] = onKeyRepeat[Display.KEY_DOWN] = 0;
				calculating = true;
				render();
				calculating = false;
				//Tuwi.log("y2 "+data[index].chunk_ypos[link_at]);
				y = data[index].chunk_ypos[link_at] - 100;
				handleEvent(22);
				// �L�[��߂�
				handleEvent(1);
				//*/
				// �A�C�R���c�k
				if(Tuwi.conf.bool("autoIconDL"))
					URLUtils.createThread(this, 17, null);
				p.close();
				mc.repaint();
			} catch (Exception e) {
				e.printStackTrace();
				p.close();
				p = new Popup(e.toString(), 5000);
			}
			break;
		case 17: // DL icons
			p = new Popup("�A�C�R���擾��...", 0);
			User.DLIcons(data);
			p.close();
			Tuwi.saveConf();
			p = new Popup("�ݒ��ۑ����܂���", 1000);
			break;
		case 19:
			handleEvent(16, url);
			break;
		case 20: // ��ړ�
			scroll(+1);
			break;
		case 21: // ���ړ�
			scroll(-1);
			break;
		case 22: // �ŏ㕔
			index = y = focus_at = link_at = 0;
			mc.repaint();
			break;
		case 23: // �ŉ���
			index = focus_at = Math.max(0, data.size() - 1);
			y = link_at = 0;
			mc.repaint();
			break;
		case 25: // �m��
			if(data.isEmpty()) return handleEvent(42);
			o = data.elementAt(focus_at);
			if(link_at == 0) {
				if(o instanceof Retweet)
					new StatusMenu(this, ((Retweet)o).RTed_status);
				else // status
					new StatusMenu(this, (Element)o);
			} else {
				String text = ((Element)o).getSplitedText()[link_at - 1];
				switch (((Element)o).getLinkType(link_at)) {
				case RichText.LINK:
					Tuwi.self.openURI(text);
					break;
				case RichText.ATREPLY:
					new StatusMenu(this, text.substring(1));
					break;
				case RichText.HASHTAG:
					Tuwi.openTab(new TimelineView(account, "��"+text, "search?"+URLUtils.urlencode(new String []{"q", text})));
				default:
					break;
				}
			}
			break;
		case 26: // ���j���[����
			handleEvent(1);
			mc.repaint();
			break;
		case 27: // 4icondl
			URLUtils.createThread(this, 17, null);
			break;
		case 30: // *���e
			Tuwi.openTab(new UpdateForm(account));
			break;
		case 40: // �ǉ��ǂݍ���(����)
		case 41: // �V���ǂݍ���(���)
		case 42: // ����ǂݍ���
			synchronized(url) {
				if(!data.isEmpty() && ev == 40) {
					handleEvent(16, url + "&m=" + ((Element)data.firstElement()).id);
				}
				if(ev == 41) {
					long id;
					if(data.isEmpty())
						id = account.read_id;
					else
						id = ((Element)data.lastElement()).id;
					//if(id == 0) id++;  // since_id=0��API�G���[�ɂȂ�
					handleEvent(16, url + "&s=" + id);
				}
				if(ev == 42) {
					handleEvent(16, url);
				}
				onKeyRelease[Display.KEY_SELECT] = 0;
				onKeyPress[Display.KEY_SELECT] = 25;
			}
			break;
		}
		return false;
	}

	// �L�[�̏�� dir = {0: �Ȃ�, 1: ��, -1: ��}
	public void scroll(int dir) {
		try {
		//*** Phase0 �]�΂ʐ�̏�
		if(data.isEmpty()) return;

		//*** Phase1 �c�ʒu�̈ړ�
		// �X�N���[�������Ɠ����L�[��������Ă��Ȃ������������薂����
		if(dir == 0 || scroll_level * dir > 0) {
			scroll_level *= 0.8;
			if(dir == 0) {
				if(scroll_level > 0) {
					dir = -1;
				} else {
					dir = 1;
				}
			}
		} else { // ksk
			scroll_level -= dir;
		}
		y += -dir * 0.01 * scroll_level * scroll_level + 0.1 * scroll_level -dir * 5;
		if(index == 0 && y < 0) {
			scroll_level = (int)Math.abs(scroll_level * 0.8);
			if(scroll_level <= 2) y = 0;
		} else if(index + 1 >= data.size() && y > 0)
			scroll_level = -(int)Math.abs(scroll_level * 0.8) - 2;

		synchronized(data) {
		//*** Phase2 �v�f�̃X�L�b�v
		int i;
		//Tuwi.log("y: "+y+" index.height:"+i);
		// �����`��̕K�v�̂Ȃ��㑤�̗v�f���΂�
		while(y > (i = ((Element)data.elementAt(index)).height)
				&& index + 1 < data.size()) {
			if(i == 0) break; // �T�C�Y���v�Z�̂����ꂠ��
			//Tuwi.log("next");
			y -= i;
			index++;
		}
		// ������
		while(y < 0 && index > 0) {
			//Tuwi.log("back");
			if((i = ((Element)data.elementAt(index - 1)).height) == 0)
				break;
			index--;
			y += i;
		}

		//*** Phase3 �����N�̈ړ�
		/**
		 * link at +1
		 *  �͈͓�?
		 *   �����N?
		 *    ����
		 *    :�ŏ���
		 *   :����data����?
		 *    ����data��
		 *    :�Ȃɂ����Ȃ�;
		 *  ����
		 *   ��ʓ���̈��?
		 *    �t�H�[�J�X�ړ�
		 *    :�Ȃɂ����Ȃ�;
		 */
		int l = link_at;
		i = focus_at;
		Element e = (Element)data.elementAt(i);
		int j = e.getLinkYpos(l);
		// �s���߂����߂�
		/*if(i < index || last <= j) {
			Tuwi.log("�߂���");
			i = index;
			l = 0;
		}
		//*/
		while(true) {
			e = (Element)data.elementAt(i);
			l -= dir;
			//Tuwi.log("l: "+l);
			if(0 <= l && l <= e.format.length) {
				if(e.getLinkType(l) > 0) {
					j = e.getLinkYpos(l);
					//Tuwi.log("j: "+j);
					if(0 <= j && j + 100 < mc.getHeight()) {
						link_at = l;
						focus_at = i;
					}
					break;
				} else {
					continue;
				}
			} else {
				i -= dir;
				//Tuwi.log("i: "+i);
				if(0 <= i && i < data.size()) {
					if(dir < 0) {
						Tuwi.log("l: -1 <= "+l);
						l = -1;
					} else {
						Tuwi.log("lformat.length: " + l);
						l = ((Element)data.elementAt(i)).format.length + 1/*name��*/;
					}
					continue;
				} else
					break;
			}
		}
		}
		//Tuwi.log("i:"+focus_at+ ", l:"+link_at);
		mc.repaint();
		} catch(Exception e) {
			Tuwi.log(e);
		}
	}

	// from http://lecture.ecc.u-tokyo.ac.jp/~cichiji/cp-03/cp-03-12-2.html
	public void sort(Vector a, int left, int right) {			// �N�C�b�N�\�[�g(����)
		if (left < right) {
			int somewhere = (left + right) / 2;		// �Ώۂ̒����ɂ�����̂���l�Ƃ��đI��
			Element pivot = (Element)a.elementAt(somewhere);			// ��l�̐ݒ�
			a.setElementAt(a.elementAt(left), somewhere);
			//a[somewhere] = a[left];			// ��l��I�񂾏ꏊ�Ɉ�ԍ��̗v�f������
			int p = left;
			for (int i = left + 1; i <= right; i++) {
				Element ai = (Element)a.elementAt(i);
				if (ai.created_at/*a[i]*/ < pivot.created_at) {			// a[i]����l��菬��������΁C
					p = p + 1;				// �l������z��C���f�b�N�Xp���v�Z���C
					swap(a, p, i);				// a[p]��a[i]����������D
				}					// a[left+1]����a[p]�܂ł͊�l��菬�����D
				//else if(ai.created_at == pivot.created_at)
				//	a.removeElementAt(i);	// �d���폜
			}
			a.setElementAt(a.elementAt(p), left);
			//a[left] = a[p];				// a[left]��a[p]����
			a.setElementAt(pivot, p);
			//a[p] = pivot;				// ��l��a[p]�ɓ����D
			/* �����a[left]����a[p-1]��a[p]�����Ca[p+1]����a[right]��a[p]�ȏ�ƂȂ�D*/

			sort(a, left, p-1);				// �����������̂ɃN�C�b�N�\�[�g��K�p
			sort(a, p+1, right);				// �����������̂ɃN�C�b�N�\�[�g��K�p
		}
	}

	protected void swap(Vector a, int i, int j) {				// �v�f�̌���
		Object tmp = a.elementAt(i);
		a.setElementAt(a.elementAt(j), i);
		a.setElementAt(tmp, j);
		// int tmp = a[i];
		// a[i] = a[j];
		// a[j] = tmp;
	}
}