package models;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import app.Tuwi;


/**
 * �u�\������v�f�v�̊��N���X���ۂ��A��
 * �^�C�����C���\���Ɏg�p�����
 * �Ԃ₫��c�l�Ȃǂ̂��߂�id�Ɠ��t�Ή�
 * height�͗v�f�̍���, text_chunk�͕�����̏W��, text_ypos�͏W���̊e�������y�ʒu
 * �t�H���g���ύX���ꂽ�ꍇheight���ς�邪�ǂ����悤���ȁE�E�E
 **/
public class Element extends RichText {
	public long id, created_at;
	
	public int height, y;
	//public String[] text_chunk;  // [screen_name, text(1), test(2), ...]
	//public int[] text_ypos;  // [text_ypos(1), test_ypos(2), ...]�����N�c�ʒu�B�`�掞�ɕۑ������
	
	public Object[] src;  // ���f�[�^
	
	//public Element prev, next;
	// �O��̃����N�̏c�ʒu��Ԃ��H
	//public int prevFocus(int f) { return -1; }
	//public int nextFocus(int f) { return -1; }
	//public Object click() { return src[1]; }
	//public void render(View v, int focus_at, int style) {
		//v.str(0, src[0].toString()).skipLine();
	//}
	//public void setHighlight(int _) {}
	
	Element(Object[] _) {
		src = _;
	}
	
	public Element(int ev, int mode, String t) {
		id = ev;
		created_at = mode;
		text = t;
	}
	
	public String getDate() {
		// ��Ύ���/���Ύ��ԁ@���{��/�p��
		// [n���O][n���Ԕ��O][�[��n��m��][nn:mm] < 24h < [nn��mm��HH:MM][nn/mm NN:MM]
		long delta = (System.currentTimeMillis() / 1000)/*now*/ - created_at;
		if(delta > 24*60*60)
			return getAbsDate();
		
		// 0: ��Ύ���, 1: ��Ύ���(�b�܂�), 2: ���Ύ���, 3: ���Ύ���(24���Ԃŕb����Ύ����\��), 3<: �Ȃ�
		int b = (int)Tuwi.conf.Long("���t����");
		if(b > 3) return "";
		int i;
		String f;
		if(b < 2) {
			Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			cal.setTime(new Date(created_at * 1000));
			i = cal.get(Calendar.HOUR_OF_DAY);
			f = (i<5? "�[��": i<10? "��": i<12? "�ߑO": i<19? "�ߌ�": "��")
				+ cal.get(Calendar.HOUR);
			i = cal.get(Calendar.MINUTE);
			f += (i<10 ? "��0" : "��") + i + "��";
			if(b == 1) {
				i = cal.get(Calendar.SECOND);
				f += (i<10 ? "0" : "") + i + "�b";
			}
		} else {
			if(delta > 3600*6)
				f = delta / 3600 + "���ԑO";
			else if(delta > 3600)
				f = delta / 3600 + "����" + (delta % 3600) / 60 + "���O";
			else if(delta > 60*10)
				f = delta / 60 + "���O";
			else
				f = delta + "�b�O";
		}
		return f;
	}
	
	public String getAbsDate() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.setTime(new Date(created_at * 1000));
		// 0: ��Ύ���, 1: ��Ύ���(�b�܂�), 2: ���Ύ���, 3: ���Ύ���(24���Ԃŕb����Ύ����\��)
		String f = (cal.get(Calendar.MONTH) + 1) + "/"
			 + cal.get(Calendar.DATE) + " "
			 + cal.get(Calendar.HOUR_OF_DAY)
			 + (cal.get(Calendar.MINUTE) < 10 ? ":0" : ":")
			 + cal.get(Calendar.MINUTE);
		if(Tuwi.conf.Long("���t����") % 2 == 1)
			f += (cal.get(Calendar.SECOND) < 10 ? ":0" : ":")
				+ cal.get(Calendar.SECOND);
		return f;
	}
	
	// w�Ԗڂ̃����N��Y�ʒu��Ԃ�
	public int getLinkYpos(int w) {
		if(w == 0) return y;
		return (format[w - 1] >> 12) & 0x0ffff;
	}
	
	//w�Ԗڂ̃����N�̎�ނ�Ԃ�
	public int getLinkType(int w) {
		if(w == 0) return RichText.LINK;
		return format[w - 1] >> 28;
	}
}
