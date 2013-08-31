package org.summercool.hsf.test.phone;

import static org.jboss.netty.channel.Channels.write;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ByteBufferBackedChannelBuffer;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.summercool.hsf.serializer.Serializer;

/**
 * @Title: CustomHsfEncoder.java
 * @Package org.summercool.hsf.test.phone
 * @Description: TODO(添加描述)
 * @date 2012-3-24 上午12:47:23
 * @version V1.0
 */
public class CustomHsfEncoder implements ChannelDownstreamHandler {
	private Serializer serializer;

	public CustomHsfEncoder(Serializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (!(e instanceof MessageEvent)) {
			ctx.sendDownstream(e);
			return;
		}

		MessageEvent event = (MessageEvent) e;
		Object originalMessage = event.getMessage();
		byte[] encodedMessage = serializer.serialize(originalMessage);

		if (encodedMessage != null) {
			ByteBufferBackedChannelBuffer channelBuffer = new ByteBufferBackedChannelBuffer(ByteBuffer.wrap(encodedMessage));
			write(ctx, e.getFuture(), channelBuffer, event.getRemoteAddress());
		}
	}
}
