package models;

import java.util.Vector;
import org.apache.regexp.RE;


public class RichText {
	// これらの定数は下の３配列のインデックスになってる
	public static final int PLAIN = 0;
	public static final int LINK = 1;
	public static final int ATREPLY = 2;
	public static final int HASHTAG = 3;
	
	// 文字色, 選択文字色, 背景色
	public static int[] color = {0x000000, 0x0000ff, 0xff0000, 0x00ff00};
	public static int[] color_h = {0x000000, 0x0000ff, 0xff0000, 0x00ff00};
	public static int[] bgcolor = {0x00000000, 0x7f0000ff, 0x7fff0000, 0x7f00ff00};
	
	// 装飾対象文字列。
	public String text;
	/** 文字列フォーマット。
	* 0xABBBBCCC
	* A := type(上記定数) 0..15
	* BBBB := 位置 0..2^16-1
	* CCC := 文字数 0..4095
	**/
	public int[] format;
	public RE urlize = new RE("(https?://[^　。）<> '\"]+|@\\w{1,15}|#\\w{1,15})");
	
	public String[] getSplitedText() {
		String[] result = new String[format.length];
		for(int i=0, pos=0; i<result.length; ++i) {
			result[i] = text.substring(pos, pos + (0xfff & format[i]));
			pos += 0xfff & format[i];
		}
		return result;
	}
	
	public void setText(String s) {
		text = s;
        // Create new vector
        Vector v = new Vector(8);

        // Start at position 0 and search the whole string
        int pos = 0;
        int len = s.length();

        // Try a match at each position
        while (pos < len && urlize.match(s, pos)) {
        	//System.out.println("RE: "+getParen(0)+" "+getParen(1)+" "+getParen(2));
        
            // Get start of match
            int start = urlize.getParenStart(0);

            // Get end of match
            int newpos = urlize.getParenEnd(0);

            // Check if no progress was made
            if (newpos == pos) {
            	//Tuwi.log("0:"+s.substring(pos, start + 1));
                v.addElement(new Integer(0xfff & (start + 1 - pos))); 
                newpos++;
            } else {
            	//Tuwi.log("1:"+s.substring(pos, start));
            	v.addElement(new Integer(0xfff & (start - pos)));
            }
            
            start = PLAIN;
            for(int i=1, l=urlize.getParenCount(); i<l; ++i) {
            	switch(text.charAt(urlize.getParenStart(i))) {
            	case '@':
            		start = ATREPLY;
            		break;
            	case '#':
            		start = HASHTAG;
            		break;
            	default:
            		start = LINK;
            	}
            	//Tuwi.log("2:"+urlize.getParen(i));
            	v.addElement(new Integer((start << 28) + (0xfff & urlize.getParenLength(i))));
            }
            
            // Move to new position
            pos = newpos;
        }

        // Push remainder if it's not empty
        s = s.substring(pos);
        if (s.length() != 0) {
        	//Tuwi.log(remainder);
        	v.addElement(new Integer(0xfff & s.length()));
        }
        
        format = new int[v.size()];
        for(int i=0; i<format.length; ++i)
        	format[i] = ((Integer)v.elementAt(i)).intValue();
	}
}
