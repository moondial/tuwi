package msgpack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * MessagePack Packer/Unpacker
 * 
 */
public class Msgpack {
	public static String encoding = "SJIS";
	private Object _;
	
	// unpacking
	public Msgpack(InputStream i) {
		_ = i;
	}
	// packing
	public Msgpack(OutputStream i) {
		_ = i;
	}
	// new Integer(0) 20
	// new Long(0) 24
	// new Object() 16
	
	/**
	* unpacker
	* @return unpacked object
	*/
	public Object getObject() throws IOException {
		InputStream in = (InputStream)_;
		byte[] o;
		int i = in.read();
		long oo;
		if (i == -1) return null;
		// fix num
		if(-31 <= (byte)i/* <= 127 */) {
			//System.out.println("Int : "+i);
			return new Long((byte)i);
		}
		//System.out.println("Int : "+i);
		// fix raw
		if((i & 0xe0) == 0xa0) {
			o = new byte[i & 0x1f];
			in.read(o);
			return new String(o, encoding);
		}
		
		// fix map
		if((i & 0xf0) == 0x80)
			return map(i & 0xf);
		
		// fix array
		if((i & 0xf0) == 0x90)
			return arr(i & 0xf);
		
		switch(i){
		// nil(null)
		case 0xc0: return null;
		
		// boolean
		case 0xc2: return Boolean.FALSE;
		case 0xc3: return Boolean.TRUE;
		
		// float/double
		case 0xca: return new Float(Float.intBitsToFloat((in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read()));
		case 0xcb: return new Double(Double.longBitsToDouble(((((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read()) << 32) + ((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read()));
		
		// uint 8/16
		case 0xcc: return new Long(in.read());
		case 0xcd: return new Long((in.read() << 8) + in.read());
		
		// uint 32
		case 0xce: return new Long(((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read());
		// uint 64
		case 0xcf:
			oo = ((((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read()) << 32) + ((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
			if(oo < 0) throw new IOException("uint64 over 63bits is not supported");
			else return new Long(oo);
		
		// int 8/16/32/64
		case 0xd0: return new Long((byte)in.read());
		case 0xd1: return new Long((short)((in.read() << 8) + in.read()));
		case 0xd2: return new Long((in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read());
		case 0xd3: return new Long(((((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read()) << 32) + ((long)in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read());
		
		// raw16(byte[])
		case 0xd8:
		// raw 16
		case 0xda:
			o = new byte[(in.read() << 8) + in.read()];
			in.read(o);
			if(i == 0xda)
				return new String(o, encoding);
			else
				return o;
			/* for UTF-8
			// bytes count into length
			j = readUnsignedShort() / 2;
			c = new char[j];
			for(j=0; j<c.length; ++j) c[j] = readChar();
			return new String(c);
			*/
		// raw 32(signed int)
		case 0xdb:
			i = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
			if(i < 0) throw new IOException("raw32 over 31bits is not supported");
			o = new byte[i];
			in.read(o);
			return new String(o, encoding);
		
		// array 16
		case 0xdc:
			return arr((in.read() << 8) + in.read());
		// array 32(signed int)
		case 0xdd:
			return arr((in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read());
		
		// map 16
		case 0xde:
			return map((in.read() << 8) + in.read());
		// map 32(signed int)
		case 0xdf:
			return map((in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read());
		
		}
		//System.out.println(i);
		return null;
	}
	
	private ExHash map(int i) throws IOException {
		if(i < 0) throw new IOException("map32 over 31bits is not supported");
		ExHash _ = new ExHash();
		try {
			while(i-- > 0)
				_.put(getObject(), getObject());
		} catch(Exception e) {e.printStackTrace();}
		return _;
	}
	
	private Object[] arr(int i) throws IOException {
		if(i < 0) throw new IOException("ary32 over 31bits is not supported");
		Object[] _ = new Object[i];
		i = 0;
		while(i < _.length) _[i++] = getObject();
		return _;
	}
	/**
	* packer
	* @param i the value to pack
	* @return this instance
	*/
	public Msgpack add(long i) throws IOException {
		OutputStream o = (OutputStream)_;
		if(i < -(1L<<5)) {
			if(i < -(1L<<15)) {
				if(i < -(1L<<31)) {
					// signed 64
					o.write(0xd3);
					o.write((int)(i>>56));
					o.write((int)(i>>48));
					o.write((int)(i>>40));
					o.write((int)(i>>32));
					o.write((int)(i>>24));
					o.write((int)(i>>16));
					o.write((int)(i>>8));
					o.write((int)i);
				} else {
					// signed 32
					o.write(0xd2);
					o.write((int)(i>>24));
					o.write((int)(i>>16));
					o.write((int)(i>>8));
					o.write((int)i);
				}
			} else {
				if(i < -(1<<7)) {
					// signed 16
					o.write(0xd1);
					o.write((int)(i>>8));
					o.write((int)i);
				} else {
					// signed 8
					o.write(0xd0);
					o.write((int)i);
				}
			}
		} else if(i < (1<<7)) {
			// fixnum
			o.write((int)i);
		} else {
			if(i < (1L<<16)) {
				if(i < (1<<8)) {
					// unsigned 8
					o.write(0xcc);
					o.write((int)i);
				} else {
					// unsigned 16
					o.write(0xcd);
					o.write((int)(i>>8));
					o.write((int)i);
				}
			} else {
				if(i < (1L<<32)) {
					// unsigned 32
					o.write(0xce);
					o.write((int)(i>>24));
					o.write((int)(i>>16));
					o.write((int)(i>>8));
					o.write((int)i);
				} else {
					// unsigned 64
					o.write(0xcf);
					o.write((int)(i>>56));
					o.write((int)(i>>48));
					o.write((int)(i>>40));
					o.write((int)(i>>32));
					o.write((int)(i>>24));
					o.write((int)(i>>16));
					o.write((int)(i>>8));
					o.write((int)i);
				}
			}
		}
		return this;
	}
	public Msgpack add(byte[] b) throws IOException {
		OutputStream o = (OutputStream)_;
		if(b.length < 65536) {
			o.write(0xd8);
			o.write(b.length>>8);
			o.write(b.length);
		} else {
			throw new IOException("raw32(byte[]) is not supported");
		}
		o.write(b);
		return this;
	}
	public Msgpack add(float i) throws IOException {
		OutputStream o = (OutputStream)_;
		int _ = Float.floatToIntBits(i);
		// float
		o.write(0xca);
		o.write(_>>24);
		o.write(_>>16);
		o.write(_>>8);
		o.write(_);
		return this;
	}
	
	public Msgpack add(double i) throws IOException {
		OutputStream o = (OutputStream)_;
		long _ = Double.doubleToLongBits(i);
		// double
		o.write(0xcb);
		o.write((int)(_>>56));
		o.write((int)(_>>48));
		o.write((int)(_>>40));
		o.write((int)(_>>32));
		o.write((int)(_>>24));
		o.write((int)(_>>16));
		o.write((int)(_>>8));
		o.write((int)_);
		return this;
	}
	
	public Msgpack add(String i) throws IOException {
		OutputStream o = (OutputStream)_;
		byte[] b = i.getBytes(encoding);
		// fix raw
		if(b.length < 32)
			o.write(0xa0 | b.length);
		// raw 16
		 else if(b.length < 65536) {
			o.write(0xda);
			o.write(b.length>>8);
			o.write(b.length);
		} else {
			o.write(0xdb);
			o.write(b.length>>24);
			o.write(b.length>>16);
			o.write(b.length>>8);
			o.write(b.length);
		}
		o.write(b);
		return this;
	}
	
	public void add(Hashtable i) throws IOException {
		OutputStream o = (OutputStream)_;
		// length
		int l = i.size();
		
		// fix map
		if(l < 16)
			o.write(0x80 | l);
		// map 16
		else if(l < 65536) {
			o.write(0xde);
			o.write(l>>8);
			o.write(l);
		// map 32
		} else {
			o.write(0xdf);
			o.write(l>>24);
			o.write(l>>16);
			o.write(l>>8);
			o.write(l);
		}
		
		Enumeration e = i.keys();
		while (e.hasMoreElements()){
			Object key = e.nextElement(); 
			add(key);
			add(i.get(key));
		}
	}
	public void add(Object i) throws IOException {
		OutputStream o = (OutputStream)_;
		if(i == null) o.write(0xc0);
		else if(i instanceof byte[]) {
			add((byte[])i);
		}else if(i.getClass().isArray()){
			// length
			int l = ((Object[])i).length;
			// fix array
			if(l < 16)
				o.write(0x90 | l);
			// array 16
			else if(l < 65536) {
				o.write(0xdc);
				o.write(l>>8);
				o.write(l);
			// array 32
			} else {
				o.write(0xdd);
				o.write(l>>24);
				o.write(l>>16);
				o.write(l>>8);
				o.write(l);
			}
			int _ = 0;
			while (_ < l)
				add(((Object[])i)[_++]);
		}else if(i instanceof Long) add(((Long)i).longValue());
		else if(i instanceof Boolean)	{
			o.write(i.equals(Boolean.TRUE) ? 0xc3 : 0xc2);
		}
		else if(i instanceof String) add((String)i);
		//else if(i instanceof Vector) add((Vector)i);
		else if(i instanceof Hashtable) add((Hashtable)i);
		else if(i instanceof Byte) add(((Byte)i).byteValue());
		else if(i instanceof Short) add(((Short)i).shortValue());
		else if(i instanceof Integer) add(((Integer)i).intValue());
		else if(i instanceof Float) add(((Float)i).floatValue());
		else if(i instanceof Double) add(((Double)i).doubleValue());
		else throw new IOException("Unknown Object Error" /*+ o.getClass() + o*/);
	}
	
	public void flush() throws IOException {
		((OutputStream)_).flush();
	}
	
	public void close() throws IOException {
		if(_ instanceof InputStream) ((InputStream)_).close();
		else ((OutputStream)_).close();
	}
}
