package neoe.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoeCode {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new NeoeCode().test();

	}

	private int getLen(int v) {
		if (v == 0)
			return 1;
		if (v > 0) {
			if (v >> 8 == 0) {
				if (((byte) (v)) < 0)
					return 2;
				return 1;
			}
			if (v >> 16 == 0) {
				if (((short) (v)) < 0)
					return 4;
				return 2;
			}
			return 4;
		} else {
			v = 0xffffffff - v;
			if (v >> 8 == 0) {
				if (((byte) (v)) < 0)
					return 2;
				return 1;
			}
			if (v >> 16 == 0) {
				if (((short) (v)) < 0)
					return 4;
				return 2;
			}
			return 4;
		}
	}

	private void test() throws Exception {
		DataOutputStream out = new DataOutputStream(new FileOutputStream(
				"c:/neoecd.1"));
		Object o1;
		encode(o1 = PyData.parseAll("{CATEGORIES:{1:1},'D\\'GM\nATTRIBS':{1:-1,2:4},GROUPS:{2:2},"
				+ "TYPES:{123.4567890123456789:2,3:[1.1,2.345,0,aaa,-123,127,128,129,255,"
				+ 0x100
				+ ","
				+ 0xffff
				+ ","
				+ 0x10000
				+ ","
				+ 0xffffffff
				+ ","
				+ 0x1ffffffffL + ",8589934591" + "," + 0xffffff + "]}}"), out);
		System.out.println(o1);
		out.close();
		Object o = decode(new DataInputStream(
				new FileInputStream("c:/neoecd.1")));
		System.out.println(o);
	}

	private Object decode(DataInputStream in) throws Exception {
		byte c = in.readByte();
		switch (c) {
		case 'f':
			return in.readFloat();
		case 'G':
			return in.readLong();
		case 'I':
			return in.readByte();
		case 'i':
			return in.readShort();
		case 'j':
			return in.readInt();
		case 'S': {
			int size = in.readByte();
			byte[] bs = new byte[size];
			in.readFully(bs);
			return new String(bs);
		}
		case 's': {
			int size = in.readShort();
			byte[] bs = new byte[size];
			in.readFully(bs);
			return new String(bs);
		}
		case 't': {
			int size = in.readInt();
			byte[] bs = new byte[size];
			in.readFully(bs);
			return new String(bs);
		}
		case 'L': {
			int size = in.readByte();
			List list = new ArrayList(size);
			for (int i = 0; i < size; i++)
				list.add(decode(in));
			return list;
		}
		case 'l': {
			int size = in.readShort();
			List list = new ArrayList(size);
			for (int i = 0; i < size; i++)
				list.add(decode(in));
			return list;
		}
		case 'a': {
			int size = in.readInt();
			List list = new ArrayList(size);
			for (int i = 0; i < size; i++)
				list.add(decode(in));
			return list;
		}
		case 'M': {
			int size = in.readByte();
			Map m = new HashMap(size);
			for (int i = 0; i < size; i++) {
				Object o1 = decode(in);
				Object o2 = decode(in);
				m.put(o1, o2);
			}
			return m;
		}
		case 'm': {
			int size = in.readShort();
			Map m = new HashMap(size);
			for (int i = 0; i < size; i++) {
				Object o1 = decode(in);
				Object o2 = decode(in);
				m.put(o1, o2);
			}
			return m;
		}
		case 'n': {
			int size = in.readInt();
			Map m = new HashMap(size);
			for (int i = 0; i < size; i++) {
				Object o1 = decode(in);
				Object o2 = decode(in);
				m.put(o1, o2);
			}
			return m;
		}
		default:
			throw new RuntimeException("unknow type:" + c);
		}
	}

	private void encode(Object doc, DataOutputStream out) throws Exception {
		if (doc instanceof Map) {
			encodeMap(doc, out);
		} else if (doc instanceof List) {
			encodeList(doc, out);
		} else if (doc instanceof Number) {
			encodeNumber(doc, out);
		} else {
			encodeString(doc, out);
		}

	}

	private void encodeNumber(Object doc, DataOutputStream out)
			throws Exception {
		// System.out.println(doc.getClass().getSimpleName() + ":" + doc);
		if (doc instanceof Float
				|| doc instanceof Double
				|| (doc instanceof BigDecimal && doc.toString().indexOf('.') >= 0)) {
			encodeFloat(((Number) doc).floatValue(), out);
		} else {
			int i = ((Number) doc).intValue();
			long l = ((Number) doc).longValue();
			int i2 = -1;
			if ((l >> 32 == 0) || (l >> 32 == i2)) {
				encodeInt(i, out);
			} else {
				encodeLong(l, out);
			}
		}

	}

	private void encodeFloat(float f, DataOutputStream out) throws Exception {
		out.write('f');
		out.writeFloat(f);
	}

	private void encodeLong(long l, DataOutputStream out) throws Exception {
		out.write('G');
		out.writeLong(l);
		System.out.println("long:" + l);
	}

	private void encodeInt(int i, DataOutputStream out) throws Exception {
		int len = getLen(i);
		if (len == 1) {
			out.write('I');
			out.writeByte(i);
			// System.out.println("i1:"+i);
		} else if (len == 2) {
			out.write('i');
			out.writeShort(i);
			// System.out.println("i2:"+i);
		} else { // len==4
			out.write('j');
			out.writeInt(i);
			// System.out.println("i4:"+i);
		}
	}

	private void encodeString(Object doc, DataOutputStream out)
			throws Exception {
		String s = (String) doc;
		byte[] bs = s.getBytes("UTF8");
		int size = bs.length;
		int len = getLen(size);
		if (len == 1) {
			out.write('S');
			out.writeByte(size);
		} else if (len == 2) {
			out.write('s');
			out.writeShort(size);
		} else { // len==4
			out.write('t');
			out.writeInt(size);
		}
		out.write(bs);
	}

	private void encodeList(Object doc, DataOutputStream out) throws Exception {
		List list = (List) doc;
		int size = list.size();
		int len = getLen(size);
		if (len == 1) {
			out.write('L');
			out.writeByte(size);
		} else if (len == 2) {
			out.write('l');
			out.writeShort(size);
		} else { // len==4
			out.write('a');
			out.writeInt(size);
		}
		for (Object o : list) {
			encode(o, out);
		}

	}

	private void encodeMap(Object doc, DataOutputStream out) throws Exception {
		Map m = (Map) doc;
		int size = m.size();
		int len = getLen(size);
		if (len == 1) {
			out.write('M');
			out.writeByte(size);
		} else if (len == 2) {
			out.write('m');
			out.writeShort(size);
		} else { // len==4
			out.write('n');
			out.writeInt(size);
		}
		for (Object o : m.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			encode(e.getKey(), out);
			encode(e.getValue(), out);
		}
	}

}
