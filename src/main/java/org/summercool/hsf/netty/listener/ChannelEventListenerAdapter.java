package org.summercool.hsf.netty.listener;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import org.summercool.hsf.netty.channel.HsfChannel;

/**
 * @Title: ChannelEventListenerAdapter.java
 * @Package org.summercool.hsf.netty.listener
 * @Description: ChannelEventListener适配类
 * @author 简道
 * @date 2011-9-29 上午11:08:05
 * @version V1.0
 */
public abstract class ChannelEventListenerAdapter implements ChannelEventListener {

	@Override
	public EventBehavior channelClosed(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e) {
		return EventBehavior.Continue;
	}

	@Override
	public EventBehavior channelConnected(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e) {
		return EventBehavior.Continue;
	}

	@Override
	public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
		return EventBehavior.Continue;
	}

	@Override
	public EventBehavior groupRemoved(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
		return EventBehavior.Continue;
	}

}
