package org.summercool.hsf.test.phone;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

import org.summercool.hsf.pojo.HandshakeAck;
import org.summercool.hsf.pojo.HandshakeFinish;
import org.summercool.hsf.pojo.HandshakeRequest;
import org.summercool.hsf.pojo.Heartbeat;
import org.summercool.hsf.serializer.Serializer;
import org.summercool.hsf.util.HandshakeUtil;

/**
 * @Title: CustomSerializer.java
 * @Package org.summercool.hsf.test.phone
 * @Description: 自定义序列化类
 * @date 2012-3-23 下午11:18:03
 * @version V1.0
 */
public class CustomSerializer implements Serializer {
	public static final String CHARSET = "UTF-8";
	public static final byte[] HEARTBEAT_BYTES = new byte[4];
	public static final byte[] EMPTY_BYTES = new byte[0];

	private ByteOrder order = ByteOrder.BIG_ENDIAN;

	@Override
	public void init() throws Exception {
	}

	@Override
	public byte[] serialize(Object object) throws Exception {
		if (object == null) {
			throw new NullPointerException();
		}
		if (object instanceof Heartbeat) {
			return HEARTBEAT_BYTES;
		}
		byte[] buffer;
		//
		if (HandshakeUtil.isInitMsg(object)) {
			buffer = encodeInitMsg(object);
		} else if (object instanceof byte[]) {
			byte[] msg = (byte[]) object;
			buffer = new byte[msg.length + 1];
			// 标识是业务消息
			buffer[0] = (byte) 1;
			//
			System.arraycopy(msg, 0, buffer, 1, msg.length);
		} else {
			throw new IllegalArgumentException();
		}

		byte[] result = new byte[buffer.length + 4];
		byte[] lenBytes = IntUtil.toBytes(buffer.length, order);
		System.arraycopy(lenBytes, 0, result, 0, IntUtil.COUNT);

		System.arraycopy(buffer, 0, result, 4, buffer.length);
		return result;
	}

	@Override
	public Object deserialize(byte[] bytes) throws Exception {
		if (bytes == null) {
			throw new NullPointerException();
		}
		//
		if (bytes.length == 0) {
			return Heartbeat.getSingleton();
		} else {
			// 握手消息
			if (bytes[0] == 0) {
				byte mark = bytes[1];
				int handshakeMsgLength = IntUtil.toInt(IntUtil.subArray(bytes, 2, 4), order);
				//
				byte[] dst = IntUtil.subArray(bytes, 6, handshakeMsgLength);
				String groupName = decode(dst);
				//
				if (mark == 0) {
					return new HandshakeRequest(groupName);
				} else if (mark == 1) {
					return new HandshakeAck(groupName);
				} else {
					return new HandshakeFinish(groupName);
				}
			} else {
				// 业务消息
				byte[] dst = IntUtil.subArray(bytes, 1, bytes.length - 1);
				return dst;
			}
		}
	}

	@Override
	public void register(Class<?> class1) {
	}

	private byte[] encodeInitMsg(Object object) throws Exception {
		// 标识是握手哪个阶段的消息
		int mark = 0;
		String groupName = null;
		if (object instanceof HandshakeRequest) {
			mark = 0;
			groupName = ((HandshakeRequest) object).getGroupName();
		} else if (object instanceof HandshakeAck) {
			mark = 1;
			groupName = ((HandshakeAck) object).getGroupName();
		} else if (object instanceof HandshakeFinish) {
			mark = 2;
			groupName = ((HandshakeFinish) object).getGroupName();
		}
		byte[] bytes = encode(groupName);
		byte[] result = new byte[bytes.length + 4 + 2];
		// 标识是握手消息
		result[0] = (byte) 0;
		// 标识是握手哪个阶段的消息
		result[1] = (byte) mark;

		//
		byte[] lenBytes = IntUtil.toBytes(bytes.length, order);
		System.arraycopy(lenBytes, 0, result, 2, lenBytes.length);
		//
		System.arraycopy(bytes, 0, result, 6, bytes.length);

		return result;
	}

	public static byte[] encode(String content) throws UnsupportedEncodingException {
		if (content == null) {
			return EMPTY_BYTES;
		}

		return content.getBytes(CHARSET);
	}

	public static String decode(byte[] bytes) throws UnsupportedEncodingException {
		if (bytes == null) {
			return null;
		}

		return new String(bytes, CHARSET);
	}
}
