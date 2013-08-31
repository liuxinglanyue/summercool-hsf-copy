package org.summercool.hsf.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @Title: ObjectSerializer.java
 * @Package org.summercool.hsf.serializer
 * @Description: JDK序列化和反序列化
 * @author 简道
 * @date 2011-11-11 下午5:13:17
 * @version V1.0
 */
public class ObjectSerializer implements Serializer {

	@Override
	public void init() throws Exception {
	}

	@Override
	public byte[] serialize(Object object) throws Exception {
		ByteArrayOutputStream outputStream = null;
		ObjectOutputStream oos = null;
		try {
			outputStream = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(outputStream);
			oos.writeObject(object);
			//
			return outputStream.toByteArray();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws Exception {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			//
			return ois.readObject();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	@Override
	public void register(Class<?> class1) {
	}

}
