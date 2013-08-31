package org.summercool.hsf.configserver.listener;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.summercool.hsf.configserver.ConfigManager;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;

/**
 * @Title: ConfigServerChannelListener.java
 * @Package org.summercool.hsf.configserver.listener
 * @Description: 
 * @author 简道
 * @date 2012-3-22 下午3:37:54
 * @version V1.0
 */
public class ConfigServerChannelListener extends ChannelEventListenerAdapter {
	@Override
	public EventBehavior groupRemoved(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
		// 解除所有订阅
		ConfigManager configManager = ConfigManager.getConfigManager();
		configManager.unsubscribe(groupName);

		return EventBehavior.Continue;
	}
}
