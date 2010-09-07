import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;

public class LogView extends View {
	static Runtime r = Runtime.getRuntime();
	static String[] logs = new String[1000];
	static int count, line, speed;
	
	static void log(String s) {
		//for(int i=0; i<s.length; ++i)
			logs[count++ % logs.length] = s;//[i];
		count %= logs.length;
		System.out.println(s);
	}
	
	private int get(int at) {
		if(logs[count] == null)
			return at;
		else if(logs.length - count > at)
			return count + at;
		return at - logs.length + count;
	}
	
	public void render(Box b) {
		b.setWidth(0, Box.auto, 0)
		.str(0, "メモリ残量 "+r.freeMemory()).newLine()
		.str(0, "メモリ総量 "+r.totalMemory()).newLine()
		.child().setHeight(0, 2, 0).rgb(0x5f5f5f).fill();
		b.slide(3);
		
		int j;
		for(j=line; j<logs.length; ++j) {
			if(logs[get(j)] == null) break;
			b.str(0, logs[get(j)]).newLine();
			if(b._y >= mc.getHeight()) break;
		}

		drawScrollBar(logs.length, line, j);
	}
	
	public boolean handleEvent(int ev, Object o) {
		switch(ev) {
		case 1: // focus
			title = "動作ログ";
			// ソフトキーのラベルを設定
			mc.setSoftLabel(Frame.SOFT_KEY_1, "ﾘｾｯﾄ");
			mc.setSoftLabel(Frame.SOFT_KEY_2, "更新");
			onKeyPress[Display.KEY_UP] = 16;
			onKeyPress[Display.KEY_DOWN] = 17;
			onKeyRepeat[Display.KEY_UP] = 16;
			onKeyRepeat[Display.KEY_DOWN] = 17;
			onKeyRelease[Display.KEY_UP] = 20;
			onKeyRelease[Display.KEY_DOWN] = 20;
			onKeyRelease[Display.KEY_SOFT1] = 18;
			onKeyRelease[Display.KEY_SOFT2] = 19;
			onKeyPress[3] = 21;
			onKeyPress[9] = 22;
			break;
		case 2: // unfocus
			break;
		case 16:
			line += speed-- / 10;
			if(--line < 0) line = 0;
			break;
		case 17:
			line += speed++ / 10;
			if(++line >= logs.length) line = logs.length - 1;
			break;
		case 20:
			speed = 0;
			break;
		case 18: // reset
			logs = new String[logs.length];
			count = line = 0;
			System.gc();
			break;
		case 19: // refresh
			System.gc();
			break;
		case 21:
			line = 0;
			break;
		case 22:
			line = logs.length - 1;
			break;
		}
		mc.repaint();
		return false;
	}
	
	public String toString() { return "Log"; }
}