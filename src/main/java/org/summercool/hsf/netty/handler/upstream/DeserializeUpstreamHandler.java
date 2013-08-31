package org.summercool.hsf.netty.handler.upstream;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.summercool.hsf.pojo.Heartbeat;
import org.summercool.hsf.serializer.KryoSerializer;
import org.summercool.hsf.serializer.Serializer;

/**
 * @Title: DeserializeUpstreamHandler.java
 * @Package org.summercool.hsf.netty.channelhandler.downstream
 * @Description: 反序列化
 * @author 简道
 * @date 2011-9-16 下午4:45:59
 * @version V1.0
 */
public class DeserializeUpstreamHandler extends SimpleChannelUpstreamHandler {
	Logger logger = LoggerFactory.getLogger(getClass());
	private Serializer serializer = new KryoSerializer();
	
	public DeserializeUpstreamHandler(){
	}
	
	public DeserializeUpstreamHandler(Serializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() == null) {
			return;
		} else if (e.getMessage() instanceof byte[]) {
			byte[] bytes = (byte[]) e.getMessage();
			Object msg;
			if (bytes.length == 0) {
				msg = Heartbeat.getSingleton();
			} else {
				try {
					msg = serializer.deserialize(bytes);
				} catch (Exception ex) {
					throw ex;
				}
			}
			UpstreamMessageEvent event = new UpstreamMessageEvent(e.getChannel(), msg, e.getRemoteAddress());
			super.messageReceived(ctx, event);
		} else {
			super.messageReceived(ctx, e);
		}
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
}