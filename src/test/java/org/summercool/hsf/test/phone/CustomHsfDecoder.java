package org.summercool.hsf.test.phone;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.summercool.hsf.netty.decoder.HsfFrameDecoder;
import org.summercool.hsf.pojo.Heartbeat;
import org.summercool.hsf.serializer.Serializer;

/**
 * @Title: CustomHsfDecoder.java
 * @Package org.summercool.hsf.test.phone
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-24 上午12:48:02
 * @version V1.0
 */
public class CustomHsfDecoder extends HsfFrameDecoder{
	public static ChannelBufferFactory BUFFER_FACTORY = HeapChannelBufferFactory.getInstance();
	private Serializer serializer;

	public CustomHsfDecoder(Serializer serializer) {
		this.serializer = serializer;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() >= 4) {
			buffer.markReaderIndex();
			int length = buffer.readInt();
			
			if (length < 0) {
				return null;
			} else if (length == 0) {
				return Heartbeat.getSingleton();
			} else if (buffer.readableBytes() >= length) {
				byte[] bytes = new byte[length];
				buffer.readBytes(bytes);
				
				//
				return serializer.deserialize(bytes);
			} else {
				buffer.resetReaderIndex();
			}
		}

		return null;
	}

}
