package org.summercool.hsf.netty.decoder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.summercool.hsf.pojo.Heartbeat;

/**
 * @ClassName: LengthBasedDecoder
 * @Description: 基于长度的解码器
 * @author 简道
 * @date 2011-9-29 下午1:42:59
 * 
 */
public class LengthBasedDecoder extends HsfFrameDecoder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private int headerFieldLength = 4;

	public LengthBasedDecoder() {
		this(4);
	}

	public LengthBasedDecoder(int headerFieldLength) {
		this.headerFieldLength = headerFieldLength;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() >= headerFieldLength) {
			buffer.markReaderIndex();
			int length = buffer.readInt();
			
			if (length < 0) {
				logger.error("msg length must >= 0. but length={}", length);
				return null;
			} else if (length == 0) {
				return Heartbeat.BYTES;
			} else if (buffer.readableBytes() >= length) {
				byte[] bytes = new byte[length];
				buffer.readBytes(bytes);

				return bytes;
			} else {
				buffer.resetReaderIndex();
			}
		}

		return null;
	}
}
