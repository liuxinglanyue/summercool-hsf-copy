package org.summercool.hsf.netty.listener;

import java.util.EventListener;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import org.summercool.hsf.netty.channel.HsfChannel;

/**
 * @Title: ChannelEventListener.java
 * @Package org.summercool.hsf.netty.listener
 * @Description: 通道事件监听类
 * @author 简道
 * @date 2011-9-27 上午11:45:50
 * @version V1.0
 */
public interface ChannelEventListener extends EventListener {

	/**
	 * Invoked when a {@link Channel} was closed and all its related resources were released.
	 * 
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param e
	 * @return EventBehavior Whether to continue the events deliver
	 */
	public EventBehavior channelClosed(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e);

	/**
	 * Invoked when a {@link Channel} is open, bound to a local address, and connected to a remote address.
	 * 
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param e
	 * @return EventBehavior Whether to continue the events deliver
	 */
	public EventBehavior channelConnected(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e);

	/**
	 * Invoked when a group is created.
	 * 
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param groupName
	 * @return EventBehavior Whether to continue the events deliver
	 */
	public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName);

	/**
	 * Invoked when a group is removed.
	 * 
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param groupName
	 * @return EventBehavior Whether to continue the events deliver
	 */
	public EventBehavior groupRemoved(ChannelHandlerContext ctx, HsfChannel channel, String groupName);
}
