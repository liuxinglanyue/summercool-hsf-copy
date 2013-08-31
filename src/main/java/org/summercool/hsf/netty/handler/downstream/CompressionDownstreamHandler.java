package org.summercool.hsf.netty.handler.downstream;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;

import org.summercool.hsf.compression.strategy.CompressionResult;
import org.summercool.hsf.compression.strategy.CompressionStrategy;
import org.summercool.hsf.compression.strategy.ThresholdCompressionStrategy;

/**
 * @Title: CompressionDownstreamHandler.java
 * @Package org.summercool.hsf.netty.channelhandler.downstream
 * @Description: 压缩处理器
 * @author 简道
 * @date 2011-9-16 下午4:45:59
 * @version V1.0
 */
public class CompressionDownstreamHandler implements ChannelDownstreamHandler {

	private CompressionStrategy compressionStrategy = new ThresholdCompressionStrategy();

	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (!(e instanceof MessageEvent)) {
			ctx.sendDownstream(e);
			return;
		}

		MessageEvent event = (MessageEvent) e;
		Object originalMessage = event.getMessage();
		if (originalMessage instanceof byte[]) {
			CompressionResult compressionResult = compressionStrategy.compress((byte[]) originalMessage);

			byte[] resBuffer = compressionResult.getBuffer();
			int length = resBuffer.length;
			byte[] bytes = new byte[length + 1];
			bytes[0] = compressionResult.isCompressed() ? (byte) 1 : (byte) 0;
//			for (int i = 0; i < length; i++) {
//				bytes[i + 1] = resBuffer[i];
//			}
			// 提高内存拷备性能，未来要减少一次内存拷备及序列化线程优化
			System.arraycopy(resBuffer, 0, bytes, 1, length);

			DownstreamMessageEvent evt = new DownstreamMessageEvent(event.getChannel(), event.getFuture(), bytes, event.getRemoteAddress());

			ctx.sendDownstream(evt);
		} else {
			ctx.sendDownstream(e);
		}
	}

	public void setCompressionStrategy(CompressionStrategy compressionStrategy) {
		this.compressionStrategy = compressionStrategy;
	}
}
