package org.summercool.hsf.test.listener;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.util.HsfContextHolder;

public class TestChannelListener extends ChannelEventListenerAdapter {

	@Override
	public EventBehavior groupRemoved(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
		//
		System.out.println(HsfContextHolder.getAttributes());
		System.out.println("group " + groupName + " is removed");
		return null;
	}

	@Override
	public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
		//
		HsfContextHolder.getAttributes().put("hello", "helsddsd");
		System.out.println("group " + groupName + " is created");
		return null;
	}

	@Override
	public EventBehavior channelConnected(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e) {
		//
		System.out.println("channel " + channel + " is connected");
		return null;
	}

	@Override
	public EventBehavior channelClosed(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e) {
		//

		System.out.println("channel " + channel + " is closed");
		return null;
	}
}
