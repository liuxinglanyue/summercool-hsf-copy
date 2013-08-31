package org.summercool.hsf.test.cache;

import org.apache.commons.lang.ObjectUtils.Null;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.pojo.RequestObject;
import org.summercool.hsf.pojo.ResponseObject;
import org.summercool.hsf.serializer.KryoSerializer;
import org.summercool.hsf.serializer.Serializer;
import org.summercool.hsf.util.HandshakeUtil;
import org.summercool.hsf.util.LangUtil;

/**
 * @Title: EhcacheSerializer.java
 * @Package org.summercool.hsf.test
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-19 下午9:23:30
 * @version V1.0
 */
public class CacheSerializer implements Serializer {

	private Serializer serializer = new KryoSerializer();

	private ChannelBufferFactory bufferFactory = HeapChannelBufferFactory.getInstance();

	private static final byte[] NULL = new byte[4];

	@Override
	public void init() throws Exception {

	}

	@Override
	public byte[] serialize(Object object) throws Exception {
		ChannelBuffer ob = ChannelBuffers.dynamicBuffer(512, bufferFactory);
		if (object instanceof RequestObject) {
			encodeRequest(object, ob);
		} else if (object instanceof ResponseObject) {
			encodeResponse(object, ob);
		} else if (HandshakeUtil.isInitMsg(object)) {
			encodeInitMsg(object, ob);
		} else {
			throw new IllegalArgumentException();
		}
		byte[] buffer = new byte[ob.readableBytes()];
		ob.readBytes(buffer);
		return buffer;
	}

	private void encodeInitMsg(Object object, ChannelBuffer ob) throws Exception {
		ob.writeByte(0);
		byte[] bytes = serializer.serialize(object);
		ob.writeInt(bytes.length);
		ob.writeBytes(bytes);
	}

	private void encodeResponse(Object object, ChannelBuffer ob) throws Exception {
		ResponseObject msg = (ResponseObject) object;
		ob.writeByte(2);
		ob.writeLong(msg.getSeq());
		//
		boolean noError = msg.getCauseMessage() == null;
		ob.writeByte(noError ? 0 : 1);
		if (noError) {
			if (msg.getTarget() != null) {
				byte[] bTarget = serializer.serialize(msg.getTarget());
				ob.writeInt(bTarget.length);
				ob.writeBytes(bTarget);
			} else { // 如果返回值为null，则走下面这段逻辑，之前没有处理所以会有bug
				byte[] bTarget = new byte[4];
				ob.writeInt(bTarget.length);
				ob.writeBytes(bTarget);
			}
		} else {
			byte[] bError = serializer.serialize(msg.getCauseMessage());
			ob.writeInt(bError.length);
			ob.writeBytes(bError);
		}
	}

	private void encodeRequest(Object object, ChannelBuffer ob) throws Exception {
		RequestObject msg = (RequestObject) object;
		ob.writeByte(1);
		ob.writeLong(msg.getSeq());
		if (msg.getTarget() instanceof RemoteServiceObject) {
			//
			RemoteServiceObject serviceObj = (RemoteServiceObject) msg.getTarget();
			//
			byte[] bServiceName = serializer.serialize(serviceObj.getServiceName());
			ob.writeByte(bServiceName.length);
			ob.writeBytes(bServiceName);
			//
			byte[] bMethod = serializer.serialize(serviceObj.getMethodName());
			ob.writeByte(bMethod.length);
			ob.writeBytes(bMethod);
			//
			Object[] args = serviceObj.getArgs();
			if (args != null) {
				ob.writeByte(args.length);
				for (Object arg : args) {
					if (arg == null) {
						ob.writeInt(0);
					} else if (arg instanceof byte[]) {
						ob.writeInt(((byte[]) arg).length + 1);
						ob.writeByte(0);
						ob.writeBytes((byte[]) arg);
					} else {
						byte[] bArg = serializer.serialize(arg);
						ob.writeInt(bArg.length + 1);
						ob.writeByte(1);
						ob.writeBytes(bArg);
					}
				}
			}
		}
	}

	private Object decodeInitMsg(ChannelBuffer buffer) throws Exception {
		int length = buffer.readInt();
		if (length == 0) {
			return null;
		}
		return serializer.deserialize(buffer.readBytes(length).array());
	}

	private Object decodeRequest(ChannelBuffer buffer) throws Exception {
		RequestObject msg = new RequestObject();
		//
		long seq = buffer.readLong();
		msg.setSeq(seq);
		//
		if (buffer.readable()) {
			RemoteServiceObject serviceObj = new RemoteServiceObject();
			msg.setTarget(serviceObj);
			//
			byte snLength = buffer.readByte();
			byte[] bServiceName = buffer.readBytes(snLength).array();
			String serviceName = (String) serializer.deserialize(bServiceName);
			serviceObj.setServiceName(serviceName);
			//
			byte mLength = buffer.readByte();
			byte[] bMethod = buffer.readBytes(mLength).array();
			String method = (String) serializer.deserialize(bMethod);
			serviceObj.setMethodName(method);
			//
			if (buffer.readable()) {
				byte argLength = buffer.readByte();
				Object[] args = new Object[argLength];
				for (int i = 0; i < argLength; ++i) {
					int length = buffer.readInt();
					if (length == 0) {
						args[i] = null;
					} else {
						byte mark = buffer.readByte();
						byte[] bytes = buffer.readBytes(length - 1).array();
						if (0 == mark) {
							args[i] = bytes;
						} else {
							args[i] = serializer.deserialize(bytes);
						}
					}
				}
				serviceObj.setArgs(args);
			}
		}
		return msg;
	}

	private Object decodeResponse(ChannelBuffer buffer) throws Exception {
		ResponseObject msg = new ResponseObject();
		//
		long seq = buffer.readLong();
		msg.setSeq(seq);
		//
		byte markNoError = buffer.readByte();
		int length = buffer.readInt();
		if (length > 0) {
			byte[] bytes = buffer.readBytes(length).array();
			if (0 == markNoError) {
				if (!(bytes.length == 4 && bytes[0] == NULL[0] && bytes[1] == NULL[1] && bytes[2] == NULL[2] && bytes[3] == NULL[3])) {
					msg.setTarget(serializer.deserialize(bytes));
				}
			} else {
				msg.setCauseMessage(LangUtil.toString(serializer.deserialize(bytes)));
			}
		}

		return msg;
	}

	@Override
	public Object deserialize(byte[] bytes) throws Exception {
		if (bytes.length == 0) {
			return null;
		}
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(bytes.length, bufferFactory);
		buffer.writeBytes(bytes);
		Object value = null;
		byte mark = buffer.readByte();
		if (0 == mark) {
			value = decodeInitMsg(buffer);
		} else if (1 == mark) {
			value = decodeRequest(buffer);
		} else {
			value = decodeResponse(buffer);
		}

		return value;
	}

	@Override
	public void register(Class<?> class1) {

	}
}
