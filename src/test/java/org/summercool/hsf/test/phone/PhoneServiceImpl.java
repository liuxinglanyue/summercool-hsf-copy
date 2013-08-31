package org.summercool.hsf.test.phone;

import java.io.UnsupportedEncodingException;

/**
 * @Title: PhoneServiceImpl.java
 * @Package org.summercool.hsf.test.phone
 * @date 2012-3-20 上午12:07:52
 * @version V1.0
 */
public class PhoneServiceImpl implements PhoneService {

	private static final byte[] HELLO;

	static {
		try {
			HELLO = "Hello ".getBytes(CustomSerializer.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] doExecute(byte[] msg) {
		//
		byte[] buffer = new byte[HELLO.length + msg.length];
		System.arraycopy(HELLO, 0, buffer, 0, HELLO.length);
		System.arraycopy(msg, 0, buffer, HELLO.length, msg.length);

		return buffer;
	}
}
