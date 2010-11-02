package app;


import com.nttdocomo.ui.Display;

/**
 *
 */
public class Popup extends View {
	private String text;
	private long expireAt;
	
	/**
	 * TODO: アニメーション表示
	 * @param ms 0ならクリアボタン押下までそれ以外なら指定ミリ秒表示
	 */
	public Popup(Object s, long ms) {
		super();
		text = s + "";
		if(ms != 0)
			ms += System.currentTimeMillis();
		expireAt = ms;
		onKeyPress[Display.KEY_IAPP] = 16;
		MainCanvas.popupPolicy = true;
		show();
	}
	
	public Popup(Object s) {
		this(s, 0);
	}
	
	public void show() {
		MainCanvas.popups.addElement(this);
		MainCanvas.self.repaint();
		Tuwi.log("Popup: " + text);
	}
	
	public void close() {
		MainCanvas.popups.removeElement(this);
		//MainCanvas.self.repaint();
		Tuwi.log("Popup " + getClass() + " を閉じました");
	}
	
	public void render(Box b) {
		b = b.child();
		int h = b.setWidth(mc.getWidth() / 50, mc.getWidth() * 90 / 100, mc.getWidth() / 50)
		.setHeight(0, Box.auto, 0)
		.measure(text, Box.SMALLER).boxHeight();
		// ゆっくりフェードアウトしていってね！
		//int a = (msgClock < 400 ? msgClock * 0xff / 400 : 0xff) << 24;
		b.x = mc.getWidth() * 3 / 100;
		b.y = mc.getHeight() - h - 20;
		b.setWidth(mc.getWidth() / 50, mc.getWidth() * 90 / 100, mc.getWidth() / 50)
		.setHeight(5, h, 5)
		.rgba(0xddccccff).fill()
		.rgb(0xaaaaff).rect()
		.str(0x000000, text, 0, Box.SMALLER);
		
		if(expireAt != 0 && expireAt <= System.currentTimeMillis())
			close();
	}

	public boolean handleEvent(int ev, Object o) {
		//Tuwi.log("Popup view ev" + ev);
		//if(ev == 3) 
		if(expireAt == 0 && ev == 16) {
			close();
			return false;
		}
		return true;
	}
}
