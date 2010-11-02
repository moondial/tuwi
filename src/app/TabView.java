package app;


import java.util.Vector;


import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.Display;

public class TabView extends View {
	public static Vector tabList = new Vector(5);  // �^�u�ꗗ
	public static int tabIndex = 0;  // ���J���Ă���^�u�̔ԍ�
	public View currentView;
	
	TabView(){
		super();
        onKeyPress[0] = 10;
        // �����Ń^�u�ړ�
        onKeyPress[Display.KEY_LEFT] = 1;
        onKeyPress[Display.KEY_RIGHT] = 2;
        onKeyPress[Display.KEY_IAPP] = 6;
        
        currentView = new Frontpage();
        tabList.addElement(currentView);
	}
	
	public void render(Box b) {
		/* JAVA����: private��local�� 3bytes/�Q�� �ߖ�B
		 * InputStream intes = input;��33�o�C�g�����̂�intes.read()��
		 * 12��ȏ���Ăяo����input.read()���ߖ�ɂȂ� */
    	
		// ��������^�u�`��
		b.setWidth(0, Box.auto, 0);
		for(int i=0; i<tabList.size(); ++i) {
			View v = (View)tabList.elementAt(i);
			b.str(v.titlecolor, v.title, i == tabIndex ? 0xFFF0F8FE : 0xFFABCDF8, v.titledeco);
		}
		b.child().rgb(0x5090EE).rect();
		
		/* TODO:�eView�ɂ܂�����
    	// �X�N���[���o�[�`��
    	if(currentView.height > 0) {
			int x = mc.getWidth() - 8/*���[x���W* /,
	    		yy = currentView.y * mc.getHeight() / currentView.height/*����y���W* /,
	    		h = mc.getHeight() * mc.getHeight() / currentView.height/*�{�b�N�X�̍���* /;
			if(h < mc.getHeight()) {
		        rgba(0xaaccccff);
		        g.fillRect(x, yy, 8, h);
		        rgb(0x9090FF);
		        g.drawLine(x, yy, x, yy + h);
		        g.drawLine(x, yy, x + 6, yy);
		        rgb(0);
		        g.drawLine(x, yy + h, x + 6, yy + h);
		        g.drawLine(x + 6, yy, x + 6, yy + h);
			}
		}
		*/
		
		currentView._x = 0;
		currentView._y = b.boxHeight();  // tab height
		//currentView.render(new Box(0, _y, mc.getWidth(), Box.auto).child());
		currentView.render(b.newLine());
	}
	
	public void key(int t, int k) {
		if(t == 3/* timer event */) {
			currentView.handleEvent(3, null);
			return;
		}
		int i = 
			t == 0? currentView.onKeyPress[k]:
			t == 1? currentView.onKeyRelease[k]:
			currentView.onKeyRepeat[k];
		if (i != 0) {
			// �����Ȃ�X���b�h�N��
			if(i > 0)
				currentView.handleEvent(i, null);
			else
				URLUtils.createThread(currentView, -i, null);
		}
		else
			handleEvent(
				t == 0? onKeyPress[k]:
				t == 1? onKeyRelease[k]:
				onKeyRepeat[k]);
		//Tuwi.log(t+" "+k);
	}
	
	public boolean handleEvent(int ev, Object o) {
		switch(ev){
		case 1:
			if(--tabIndex < 0) tabIndex = tabList.size()-1;
			currentView.handleEvent(2);  // on unfocus
			handleEvent(3);
			break;
		case 2:
			if(++tabIndex >= tabList.size()) tabIndex = 0;
			currentView.handleEvent(2);  // on unfocus
			// handleEvent(3);
			// break;
		case 3: // set current tab
			o = currentView;
			currentView = ((View)tabList.elementAt(tabIndex));
			if(o != currentView)
				currentView.handleEvent(1);  // on focus
			break;
		case 4: // openTab
			for(ev=0; ev<tabList.size(); ++ev) {
				if(tabList.elementAt(ev).toString().equals(o.toString())) {
					tabIndex = ev;
					handleEvent(3);
					return false;
				}
			}
			tabList.addElement(o);
			tabIndex = tabList.size()-1;
			handleEvent(3);
			break;
		case 6: // ����
			if(Tuwi.dialog(Dialog.DIALOG_YESNO | Dialog.DIALOG_INFO, "", "���܂����H") == Dialog.BUTTON_YES)
				o = currentView;
			else break;
		case 5: // close tab
			tabList.removeElement(o);
			if(currentView.equals(o) && --tabIndex < 0) tabIndex = -1;
			handleEvent(3);
			break;
		default:
			return true;
		}
		//Tuwi.log("event: " + ev);
		mc.repaint();
		return true;
	}
}
