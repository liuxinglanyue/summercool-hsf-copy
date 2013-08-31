package org.summercool.hsf.netty.handler;

import java.net.SocketTimeoutException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.pojo.Heartbeat;

/**
 * @Title: StateCheckChannelHandler.java
 * @Package org.summercool.hsf.netty.channelhandler
 * @Description: 通道状态检测
 * @author 简道
 * @date 2011-9-17 上午10:12:13
 * @version V1.0
 */
public class StateCheckChannelHandler extends IdleStateAwareChannelHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private HsfService hsfService;

	public StateCheckChannelHandler(HsfService hsfService) {
		this.hsfService = hsfService;
	}

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		if (e.getState() == IdleState.WRITER_IDLE) {
			HsfChannel hsfChannel = hsfService.getChannels().get(e.getChannel().getId());
			if (hsfChannel != null) {
				hsfChannel.write(Heartbeat.getSingleton());
			} else {
				logger.warn("writer idle on channel({}), but hsfChannel is not managed.", e.getChannel());
			}
		} else if (e.getState() == IdleState.READER_IDLE) {
			logger.error("channel:{} is time out.", e.getChannel());
			handleUpstream(ctx, new DefaultExceptionEvent(e.getChannel(), new SocketTimeoutException(
					"force to close channel(" + ctx.getChannel().getRemoteAddress() + "), reason: time out.")));

			e.getChannel().close();
			//
			HsfChannel hsfChannel = hsfService.getChannels().get(e.getChannel().getId());
			if (hsfChannel != null) {
				HsfChannelGroup channelGroup = hsfChannel.getChannelGroup();
				if (channelGroup != null) {
					channelGroup.remove(hsfChannel);
					//
					synchronized (channelGroup) {
						channelGroup = hsfService.getGroups().get(channelGroup.getName());
						if (channelGroup != null && channelGroup.isEmpty()) {
							hsfService.getEventDispatcher().dispatchGroupRemovedEvent(ctx, hsfChannel, channelGroup.getName());
						}
					}
				}
			}
		}
		super.channelIdle(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() == Heartbeat.getSingleton()) {
			HsfChannel hsfChannel = hsfService.getChannels().get(e.getChannel().getId());
			if (hsfChannel != null) {
				long now = System.currentTimeMillis();
				hsfChannel.getHeartBeatStatistic().getReceivedNum().incrementAndGet();
				hsfChannel.getHeartBeatStatistic().setLastestReceived(now);
				//
				HsfChannelGroup group = hsfChannel.getChannelGroup();
				if (group != null) {
					group.getHeartBeatStatistic().getReceivedNum().incrementAndGet();
					group.getHeartBeatStatistic().setLastestReceived(now);
				}
			}
			return;
		}
		super.messageReceived(ctx, e);
	}
}
