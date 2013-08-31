package org.summercool.hsf.netty.handler.upstream;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.summercool.hsf.compression.strategy.CompressionStrategy;
import org.summercool.hsf.compression.strategy.ThresholdCompressionStrategy;

/**
 * @Title: DecompressionUpstreamHandler.java
 * @Package org.summercool.hsf.netty.channelhandler.downstream
 * @Description: 解压缩处理器
 * @author 简道
 * @date 2011-9-16 下午4:45:59
 * @version V1.0
 */
public class DecompressionUpstreamHandler extends SimpleChannelUpstreamHandler {
	private CompressionStrategy compressionStrategy = new ThresholdCompressionStrategy();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof byte[]) {
			byte[] bytes = (byte[]) e.getMessage();
			int length = bytes.length;
			if (length > 0) {
				byte[] buffer = new byte[length - 1];
//				for (int i = 1; i < length; i++) {
//					buffer[i - 1] = bytes[i];
//				}
				System.arraycopy(bytes, 1, buffer, 0, length - 1);

				if (bytes[0] == 1) {
					buffer = compressionStrategy.decompress(buffer);
				}

				UpstreamMessageEvent event = new UpstreamMessageEvent(e.getChannel(), buffer, e.getRemoteAddress());

				super.messageReceived(ctx, event);
				return;
			}
		}
		super.messageReceived(ctx, e);
	}

	public void setCompressionStrategy(CompressionStrategy compressionStrategy) {
		this.compressionStrategy = compressionStrategy;
	}
}
