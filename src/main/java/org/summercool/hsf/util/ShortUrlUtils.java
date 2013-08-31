package org.summercool.hsf.util;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @Title: ShortUrlUtils.java
 * @Package com.gexin.platform.util
 * @Description: chars2ints或ints2chars
 * @author 简道
 * @date 2011-11-10 下午3:32:14
 * @version V1.0
 */
public class ShortUrlUtils {

//	private static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();

	private static String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; 

	public static String encoding(long num) {
		if (num < 1) {
			throw new RuntimeException("num must be greater than 0.");
		}
		StringBuilder sb = new StringBuilder();
		for (; num > 0; num /= 62) {
			sb.append(ALPHABET.charAt((int) (num % 62)));
		}
		return sb.toString();
	}

	public static long decoding(String str) {
		str = str.trim();
		if (str.length() < 1) {
			throw new RuntimeException("str must not be empty.");
		}
		long result = 0;
		for (int i = 0; i < str.length(); i++) {
			result += (long) (ALPHABET.indexOf(str.charAt(i)) * Math.pow(62, i));
		}
		return result;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		
		UUID uuid = UUID.randomUUID();
		
		System.out.println(uuid.toString());
		System.out.println(uuid.toString().replaceAll("-", ""));
		
		long most = Math.abs(uuid.getMostSignificantBits());
		long least = Math.abs(uuid.getLeastSignificantBits());
		System.out.println(encoding(most) + encoding(least));
		
		long time=20120702;
		long timeH = 2012070214;
		System.out.println(encoding(time));
		System.out.println(encoding(timeH));
		
		System.out.println(decoding("SJQM1"));
		System.out.println(decoding("ONRAC2"));

//		System.out.println(UUID.randomUUID().toString());
//		
//		long id = 90000000000L;
//		
//		System.out.println(encoding(id));
//		System.out.println(decoding("T9C4"));
		
//		for (int j = 0; j < 10000; j++) {
//			new Thread() {
//				public void run() {
//
//					String encoding;
//					long decoding;
//					for (int i = 0; i < 100; i++) {
//						UUID uuid = UUID.randomUUID();
//						long input = Math.abs(uuid.getMostSignificantBits());
//						encoding = ShortUrlUtils.encoding(input);
//						//System.out.println(input);
//						//
//						Long value = map.get(encoding);
//						if (value != null) {
//							System.out.println("有重复!");
//						}
//						map.put(encoding, 1L);
//						//
//						// System.out.println("Base62 InputCode	: " + input);
//						//
//						// System.out.println("Base62 Encoding	: " + encoding);
//						//
//						// decoding = ShortUrlUtils.decoding(encoding);
//						// System.out.println("Base62 Decoding	: " + decoding);
//					}
//
//				};
//			}.start();

//		}
	}
}
