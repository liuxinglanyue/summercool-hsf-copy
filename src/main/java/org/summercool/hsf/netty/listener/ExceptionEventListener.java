package org.summercool.hsf.netty.listener;

import java.util.EventListener;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * @Title: ExceptionEventListener.java
 * @Package org.summercool.hsf.netty.listener
 * @Description: 异常监听接口
 * @author 简道
 * @date 2011-9-27 上午11:48:09
 * @version V1.0
 */
public interface ExceptionEventListener extends EventListener {

	/**
	 * Invoked when an exception was raised by an I/O thread or a {@link ChannelHandler}.
	 */
	public EventBehavior exceptionCaught(ChannelHandlerContext ctx, Channel channel, ExceptionEvent e);
}