package org.summercool.hsf.netty.handler.downstream;

import static org.jboss.netty.channel.Channels.write;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.summercool.hsf.pojo.Heartbeat;
import org.summercool.hsf.serializer.KryoSerializer;
import org.summercool.hsf.serializer.Serializer;

/**
 * @Title: SerializeDownstreamHandler.java
 * @Package org.summercool.hsf.netty.channelhandler.downstream
 * @Description: 序列化
 * @author 简道
 * @date 2011-9-16 下午4:45:59
 * @version V1.0
 */
public class SerializeDownstreamHandler implements ChannelDownstreamHandler {
	private Serializer serializer = new KryoSerializer();

	public SerializeDownstreamHandler() {
	}

	public SerializeDownstreamHandler(Serializer serializer) {
		this.serializer = serializer;
	}

	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (!(e instanceof MessageEvent)) {
			ctx.sendDownstream(e);
			return;
		}

		MessageEvent event = (MessageEvent) e;
		Object originalMessage = event.getMessage();
		Object encodedMessage = originalMessage;

		if (!(originalMessage instanceof Heartbeat)) {
			encodedMessage = serializer.serialize(originalMessage);
		} else {
			encodedMessage = Heartbeat.BYTES;
		}

		if (originalMessage == encodedMessage) {
			ctx.sendDownstream(e);
		} else if (encodedMessage != null) {
			write(ctx, e.getFuture(), encodedMessage, event.getRemoteAddress());
		}
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
}
