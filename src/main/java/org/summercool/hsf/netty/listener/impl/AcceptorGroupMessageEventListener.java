package org.summercool.hsf.netty.listener.impl;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.channel.RoundChannelGroup;
import org.summercool.hsf.netty.event.EventDispatcher;
import org.summercool.hsf.netty.handshake.AcceptorHandshakeProcessor;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.listener.MessageEventListener;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.pojo.HandshakeAck;
import org.summercool.hsf.pojo.HandshakeFinish;
import org.summercool.hsf.pojo.HandshakeRequest;
import org.summercool.hsf.util.HandshakeUtil;

/**
 * @Title: AcceptorGroupMessageEventListener.java
 * @Package org.summercool.hsf.netty.listener
 * @Description: 服务端握手消息监控类
 * @author 简道
 * @date 2012-2-22 上午08:33:52
 * @version V1.0
 */
public class AcceptorGroupMessageEventListener extends ChannelEventListenerAdapter implements MessageEventListener {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private EventDispatcher eventDispatcher;

	public AcceptorGroupMessageEventListener(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public EventBehavior channelConnected(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e) {
		// start timer
		HandshakeUtil.resetHandshakeTimeout(channel);

		return EventBehavior.Continue;
	}

	@Override
	public EventBehavior messageReceived(ChannelHandlerContext ctx, HsfChannel channel, MessageEvent e) {
		//
		if (e.getMessage() instanceof HandshakeRequest) {
			processRequest(ctx, channel, (HandshakeRequest) e.getMessage());
		} else if (e.getMessage() instanceof HandshakeFinish) {
			processFinish(ctx, channel, (HandshakeFinish) e.getMessage());
		}

		return EventBehavior.Continue;
	}

	private void processFinish(ChannelHandlerContext ctx, HsfChannel channel, HandshakeFinish message) {
		// cancel timeout
		HandshakeUtil.cancelHandshakeTimeout(channel);

		logger.warn("received handshake finish({}) from channel({})", message, channel);
		//
		String groupName = message.getGroupName();
		HsfService service = eventDispatcher.getService();

		//
		HsfChannelGroup channelGroup = service.getGroups().get(groupName);
		if (channelGroup == null) {
			logger.error("received {} from channel {} but group is not created. force to close channel", message,
					channel);
			channel.close();
			return;
		}
		//
		if (channelGroup.setPrepared(true)) {
			AcceptorHandshakeProcessor handshakeProcessor = ((HsfAcceptor) eventDispatcher.getService())
					.getHandshakeProcessor();
			if (handshakeProcessor != null) {
				try {
					handshakeProcessor.process(message);
				} catch (Exception ex) {
					eventDispatcher.dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
				}
			}
		}
	}

	private void processRequest(ChannelHandlerContext ctx, HsfChannel channel, HandshakeRequest message) {
		// cancel timeout
		HandshakeUtil.cancelHandshakeTimeout(channel);

		logger.warn("received handshake request({}) from channel({})", message, channel);
		//
		String groupName = message.getGroupName();
		HsfService service = eventDispatcher.getService();

		//
		boolean isCreate = false;
		HsfChannelGroup channelGroup = service.getGroups().get(groupName);
		if (channelGroup == null) {
			channelGroup = new RoundChannelGroup(groupName);
			HsfChannelGroup preGroup = service.getGroups().putIfAbsent(groupName, channelGroup);
			if (!(isCreate = (preGroup == null))) {
				channelGroup = preGroup;
			}
		}

		// add channel to group
		channelGroup.add(channel);

		AcceptorHandshakeProcessor handshakeProcessor = ((HsfAcceptor) eventDispatcher.getService())
				.getHandshakeProcessor();
		// dispatch group created event
		if (isCreate) {
			if (handshakeProcessor != null) {
				try {
					Map<String,Object> initAttrMap = handshakeProcessor.getInitAttributes();
					if (initAttrMap != null) {
						channelGroup.getAttributes().putAll(initAttrMap);
					}
					//
					handshakeProcessor.process(message);
				} catch (Exception ex) {
					eventDispatcher.dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
				}
			}
			//
			eventDispatcher.dispatchGroupCreatedEvent(ctx, channel, groupName);
		}
		// write handshake ack
		HandshakeAck ack = new HandshakeAck(service.getGroupName());
		if (handshakeProcessor != null) {
			ack.setAttachment(handshakeProcessor.getAckAttachment());
		}
		//
		logger.warn("send handshake ack({}) to channel({})", ack, channel);
		// start timer
		HandshakeUtil.resetHandshakeTimeout(channel);
		//
		channel.write(ack);
	}
}
