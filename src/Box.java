import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.MediaImage;


/**
 * Box Model ������
 */
public class Box {
	static int SMALLER=-4, LARGER=+4, BOLD=1, ITALIC=2, BOLDITALIC=3,
				UNDERLINE=0x100, OVERLINE=0x200, MIDDLELINE=0x400,
				�E����=0x10000, ��������=0x20000, NOBREAK=0x40000;
	static Graphics g;
	static int auto = Integer.MAX_VALUE;
	static int �ŏ��� = 50;
	
	
	private Box parent;
	public int ��, �E, ��, ��, // margin
				��, ����, // inner box size
				_x,	_y, // temp vers for rendering
				_h, // line-height
				x, y; // distance from display origin(0,0)
	private Font font = Font.getDefaultFont();
	
	public Box() {
	}
	
	public Box(int X, int Y, int w, int h) {
		_x = x = X;
		_y = y = Y;
		�� = w;
		���� = h;
	}
	
	public Box info(String id) {
		Tuwi.log(id+">> x: "+x+", y: "+y+", _x: "+_x+", _y: "+_y+", _h: "+_h+", boxW: "+boxWidth()+", boxH: "+boxHeight());
		return this;
	}
	
	public int boxWidth() {
		if(�� == auto)
			return parent.x + parent.�� + parent.�� - x;
		return �� + �� + �E;
	}
	
	public int boxHeight() {
		if(���� == auto)
			return _y + _h - y + ��;
		return �� + ���� + ��;
	}
	
	public Box child() {
		Box c = new Box(_x, _y, auto, auto);
		c.parent = this;
		return c;
	}
	
	/**
	 * auto �w��Ŏ����v�Z
	 * @param left ���}�[�W��
	 * @param width �{�b�N�X��
	 * @param right �E�}�[�W��
	 * @return ���\�b�h�`�F�[����
	 */
	public Box setWidth(int left, int width, int right) {
		�� = left;
		�� = width;
		�E = right;
		if(width == auto) {
			if((parent.�� - parent._x) < �� + �E + �ŏ���)
				parent.newLine();
			�� = (parent.�� - parent._x) - �� - �E;
		} else {
			if((parent.�� - parent._x) < �� + �E + ��)
				parent.newLine();
			if(�� != �E)
				if(�� == auto)
					�� = (parent.�� - parent._x) - �� - �E;
				else
					�E = (parent.�� - parent._x) - �� - ��;
		}
		_x = x + ��;
		return this;
	}
	
	// �����ȊO�ɂ�auto�ݒ�s��
	public Box setHeight(int top, int height, int bottom) {
		�� = top;
		���� = height;
		�� = bottom;
		_y = y + ��;
		if(height == auto)
			height = _y - y;
		if(parent._h < ��+height+��)
			parent._h = ��+height+��;
		return this;
	}
	
	public Box slide(int _) {
		_y += _;
		return this;
	}
	
	// newLine��_h��str(0, "");���Ă���g�����ƁB
	public Box newLine() {
		// ���̍s��
		_y += _h;
		// �s�̍�����߂�
		_h = font.getHeight();
		// ���[�ɖ߂�
		_x = x + ��;
		// method chain
		return this;
	}
	
	// �ȒP������`��
	public Box str(int hex, String s) {
		return str(hex, s, 0, 0);
	}
	
	// �g��������`��(�����F[0xrrggbb]�A�����A�w�i�F[0xaarrggbb]�A����[consts]);
	//public Box str(int color, String str, int bg, int dec) {
	//	return str(color, str, bg, dec);
	//}
	//TODO: child()���̊g�傪parent��_h�ɉe������悤�ɂ��� refrect2p()
	//TODO: �s�̍����̎w��Anobreak
	// �g��������`��֐�(�����F[0xrrggbb]�A�����A�w�i�F[0xaarrggbb]�A����[consts]);
    public Box str(int color, String str, int bg, int dec) {
    	String sub;
		int _w = 0, startAt = 0, endAt = 0, len = str.length();
		// �t�H���g�ݒ�
		dec += 4;
		font = MainCanvas.fonts[dec & 0xff];
		g.setFont(font);
		// �s�̍�����ݒ�
		if (font.getHeight() > _h)
			_h = font.getHeight();
		// ���s�K�v?
		if (�� <= _x - �� - x || (dec & NOBREAK) != 0 && x + �� + �� - _x < font.getBBoxWidth(str))
			newLine();
		// �L�����Z�����Ȃ��ƕ����
		if(len == 0)
			return this;
		// �ԋ߂̉��s�ʒu
		int next = str.indexOf('\n');
		rgb(color);
		while (len > 0) {
			// ������
			startAt = endAt;
			endAt = font.getLineBreak(str, startAt, len, x + �� + �� - _x);
			
			// ���s�����݂���
			if(startAt <= next && next < endAt) {
				endAt = ++next;
				next = str.indexOf('\n', endAt);
			}
			
			//Tuwi.log("index="+next+" "+endAt);
			sub = str.substring(startAt, endAt);
			//Tuwi.log("|"+sub+";");
			// _w �͕�����
			_w = font.getBBoxWidth(sub);
			if((dec & �E����) != 0)
				_x = x + �� + �� - _w;
			// �w�i�`��
			if(bg != 0) {
				rgba(bg);
				//if(boxes != null) boxes.addElement(new Box(_x, _y, _w, font.getHeight()));
				g.fillRect(_x, _y, _w, font.getHeight());
				rgb(color);
			}
			// �����`��
			g.drawString(sub, _x, _y + font.getAscent());
			// �������� _w �͍s�͂��߂̈ʒu���當���񖖒[�܂ł̕�
			_w += _x;
			
			if(dec != 4) {
				// ����
				if ((dec & UNDERLINE) != 0)
					g.drawLine(_x, _y + font.getAscent(), _w, _y + font.getAscent());
				// ���
				if ((dec & OVERLINE) != 0)
					g.drawLine(_x, _y, _w, _y);
				// ��������
				if ((dec & MIDDLELINE) != 0)
					g.drawLine(_x, _y + font.getAscent() / 2, _w, _y + font.getAscent() / 2);
			}
			// ���̍s��
			_y += _h;
			// �s�̍�����߂�
			_h = font.getHeight();
			// ���[�ɖ߂�
			_x = x + ��;
			// �c�蕶����
			len -= endAt - startAt;
			//Tuwi.log("str"+len);
		}
		// �����̉E�[�̍��W
		_x = _w;
		// ��s�����߂��Ă��킹��
		_y -= font.getHeight();
		// ���\�b�h�`�F�[��
		return this;
	}
    
    // �`�悵�Ȃ���str()
    public Box measure(String str, int dec) {
		int _w = 0, startAt = 0, endAt = 0, len = str.length();
		dec += 4;
		font = MainCanvas.fonts[dec & 0xff];
		g.setFont(font);
		// �s�̍�����ݒ�
		if (font.getHeight() > _h)
			_h = font.getHeight();
		// ���s�K�v?
		if (�� <= _x - �� - x || (dec & NOBREAK) != 0 && x + �� + �� - _x < font.getBBoxWidth(str))
			newLine();
		// �L�����Z�����Ȃ��ƕ����
		if(len == 0)
			return this;
		// �ԋ߂̉��s�ʒu
		int next = str.indexOf('\n');
		while (len > 0) {
			// ������
			startAt = endAt;
			endAt = font.getLineBreak(str, startAt, len, �� - (_x - �� - x));
			
			// ���s�����݂���
			if(startAt <= next && next < endAt) {
				endAt = ++next;
				next = str.indexOf('\n', endAt);
			}
			// _w �͕�����+�s�͂��߂̈ʒu����
			_w = font.getBBoxWidth(str.substring(startAt, endAt)) + _x;
			// ���̍s��
			_y += _h;
			// �s�̍�����߂�
			_h = font.getHeight();
			// ���[�ɖ߂�
			_x = x + ��;
			// �c�蕶����
			len -= endAt - startAt;
			//Tuwi.log("str"+len);
		}
		// �����̉E�[�̍��W
		_x = _w;
		// ��s�����߂��Ă��킹��
		_y -= font.getHeight();
		return this;
    }
    
	// �g��������`��֐�(�����F[0xrrggbb]�A���x���A���E���F[0xaarrggbb]�A����[consts]);
    public Box button(int color, String str, int bc, int dec) {
    	int _w = 0;
		font = MainCanvas.fonts[(dec + 4) & 0xff];
		g.setFont(font);
		
		// �{�^�����s���ɂ����܂�Ȃ��ꍇ
		if(font.stringWidth(str) > �� - (_x - �� - x) && _x != x + ��)
			newLine();
		// �{�^�������s�ɂ����܂�ꍇ
		if((_w = font.stringWidth(str)) <= ��) {
    		// �O���f�ɒu�������\��
			rgba(0x80fafaff);
    		g.fillRect(_x, _y, _w + font.getHeight(), font.getHeight());
    		rgba(bc);
    		g.drawRect(_x, _y, _w + font.getHeight(), font.getHeight());
    		_x += font.getHeight() / 2;
    		rgb(color);
    		str(color, str, 0, dec);
    	} else {
    	// �����s�{�^���`��
    		Box b = child().setWidth(font.getHeight()/2, (int)(�� * 0.75), font.getHeight()/2).setHeight(0, auto, 0)
    		.measure(str, dec).rgba(0x80fafaff).fill().rgba(bc).rect();
    		_h = b.boxHeight();
    		_x = b.x + b.boxWidth();
    		b._x = b.x + b.��;
    		b._y = b.y + b.��;
    		b.str(color, str, 0, dec);
    	}
		_x += font.getHeight() / 2;
    	return this;
    }
    
	// �z�F�ύX�֗����]�b�h
	public Box rgb(int hex) {
		g.setColor(Graphics.getColorOfRGB(hex >> 16 & 0xff, hex >> 8 & 0xff, hex & 0xff));
		return this;
	}
	
	// �z�F�ύX�֗����]�b�h
	public Box rgba(int hex) {
		g.setColor(Graphics.getColorOfRGB(hex >> 16 & 0xff, hex >> 8 & 0xff, hex & 0xff, hex >> 24 & 0xff));
		return this;
	}
	
	public Box rgba2(boolean b, int link, int focus) {
		if(b) rgba(focus); else rgba(link);
		return this;
	}
	
	public Box rect() {
		g.drawRect(x, y, boxWidth(), boxHeight());
		return this;
	}
	
	public Box fill() {
		g.fillRect(x, y, boxWidth(), boxHeight());
		return this;
	}
	
	public Box clip() {
		g.setClip(x, y, boxWidth(), boxHeight());
		return this;
	}
	
	public Box parent() {
		g.clearClip();
		return this.parent;
	}
	
	public void clear() {
		g.clearRect(x, y, boxWidth(), boxHeight());
	}
	
	/*public boolean hit(int X, int Y) {
		if(y <= Y && Y <= y+��+����+�� && x <= X && X <= x+��+��+�E)
			return true;
		return false;
	}*/
	
	//public int[] capture(int[] arg) {
	//	return g.getRGBPixels();
	//}
	
	//public Box restore(int[] arg) {
	//	g.setRGBPixels();
	//	return this;
	//}
	
	public Box img(MediaImage m) {
		if(m != null)
			g.drawImage(m.getImage(), _x, _y);
		return this;
	}
	
	public Box img(MediaImage m, int w, int h) {
		if(m != null)
			g.drawScaledImage(m.getImage(), _x, _y, w, h, 0, 0, m.getWidth(), m.getHeight());
		_x += w;
		if(_h < h) _h = h;
		return this;
	}
	
	public Box chip() {
		return this;
	}
	
	// text_chunk��start_at�Ԗڂ���`��Bfocus_at�Ԗڂ̂Ƃ��I��F�ŕ`��Bchunk_ypos�ɉ�ʂ�y���W��ۑ�
	public Box bodystr(String[] text_chunk, int[] chunk_ypos, int start_at, int focus_at) {
		for (int j = start_at; j < text_chunk.length; ++j) {
			char c = text_chunk[j].charAt(0);
			if (c == ' ') {
				str(0, text_chunk[j].substring(1));
				continue;
			} else if (c == '@') {
				if (focus_at == j)
					str(0xFF6347, text_chunk[j], 0x7fFEEEF1, 1);
				else
					str(0xFF6347, text_chunk[j], 0, 0);
			} else if (c == '#') {
				if (focus_at == j)
					str(0x006400, text_chunk[j], 0x7ff0fff0, 1);
				else
					str(0x006400, text_chunk[j], 0, 0);
			} else { // 'h'
				if (focus_at == j)
					str(0x8888ff, text_chunk[j], 0x7ff5fffa, 1);
				else
					str(0x6666ff, text_chunk[j], 0, 0);
			}
			
			chunk_ypos[j] = _y;
		}
		return this;
	}
	// �{���̓C�x���g�Ή��Ƃ�����肽���̂����B�B�B
	public Box linkstr(String s, int color, int focus, int bgcolor, boolean hasfocus) {
		if(hasfocus)
			str(focus, s, bgcolor, 1);
		else
			str(color, s, 0, 0);
		return this;
	}
	// TODO: v/h grads
}
