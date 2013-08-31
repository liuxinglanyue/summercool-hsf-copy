package org.summercool.hsf.netty.encoder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @ClassName: LengthBasedEncoder
 * @Description: 基于长度的编码器
 * @author 简道
 * @date 2011-9-29 下午1:43:41
 * 
 */
public class LengthBasedEncoder extends ObjectEncoder {
	private final int estimatedLength;

	public LengthBasedEncoder() {
		this(512);
	}

	public LengthBasedEncoder(int estimatedLength) {
		if (estimatedLength < 0) {
			throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
		}
		this.estimatedLength = estimatedLength;
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg instanceof byte[]) {
			byte[] bytes = (byte[]) msg;

			ChannelBuffer ob = ChannelBuffers.dynamicBuffer(estimatedLength, channel.getConfig().getBufferFactory());

			ob.writeInt(bytes.length);
			ob.writeBytes(bytes);

			return ob;
		} else {
			throw new IllegalArgumentException("msg must be a byte[], but " + msg);
		}
	}
}
