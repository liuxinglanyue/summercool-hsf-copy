package org.summercool.hsf.test.phone;

import java.nio.ByteOrder;

/**
 * @Title: IntUtil.java
 * @Package org.summercool.hsf.test.phone
 * @Description: 实现Int的编解码
 * @date 2012-3-24 上午1:03:27
 * @version V1.0
 */
public class IntUtil {
	public static final int COUNT = 4;

	public static byte[] toBytes(int value, ByteOrder order) {
		byte[] result = new byte[COUNT];
		if (ByteOrder.BIG_ENDIAN.equals(order)) {
			int i = COUNT - 1;
			do {
				result[i] = (byte) (value % 256);
				value = (int) (value / 256);
				--i;
			} while (i >= 0);
		} else {
			int i = 0;
			do {
				result[i] = (byte) (value % 256);
				value = (int) (value / 256);
				++i;
			} while (i < COUNT);
		}
		return result;
	}

	public static int toInt(byte[] bytes, ByteOrder order) {
		if (bytes == null) {
			throw new NullPointerException();
		}
		byte[] temp = subArray(bytes, 0, COUNT);
		if (temp.length < COUNT) {
			temp = concat(new byte[COUNT - temp.length], temp);
		}

		int result = 0;
		int multiple = 1;
		if (ByteOrder.BIG_ENDIAN.equals(order)) {
			for (int i = COUNT - 1; i >= 0; i--) {
				result += temp[i] * multiple;
				multiple *= 256;
			}
		} else {
			for (int i = 0; i < COUNT; i++) {
				result += temp[i] * multiple;
				multiple *= 256;
			}
		}
		return result;
	}

	public static byte[] subArray(byte[] src, int pos, int length) {
		if (src == null || src.length < length - pos) {
			throw new IllegalArgumentException();
		}
		//
		byte[] result = new byte[length];
		System.arraycopy(src, pos, result, 0, length);
		return result;
	}

	public static byte[] concat(byte[] src1, byte[] src2) {
		//
		byte[] result = new byte[src1.length + src2.length];
		System.arraycopy(src1, 0, result, 0, src1.length);
		System.arraycopy(src2, 0, result, src1.length, src2.length);
		return result;
	}

}
