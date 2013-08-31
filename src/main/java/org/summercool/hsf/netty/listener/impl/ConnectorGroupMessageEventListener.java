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
import org.summercool.hsf.netty.handshake.ConnectorHandshakeProcessor;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.listener.MessageEventListener;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.pojo.HandshakeAck;
import org.summercool.hsf.pojo.HandshakeFinish;
import org.summercool.hsf.pojo.HandshakeRequest;
import org.summercool.hsf.util.HandshakeUtil;

/**
 * 客户端握手消息监听类
 * 
 * @Title: ConnectorGroupMessageEventListener.java
 * @Package org.summercool.hsf.netty.listener
 * @author 简道
 * @date 2012-2-22 上午11:00:48
 * @version V1.0
 */
public class ConnectorGroupMessageEventListener extends ChannelEventListenerAdapter implements MessageEventListener {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private EventDispatcher eventDispatcher;

	public ConnectorGroupMessageEventListener(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public EventBehavior channelConnected(ChannelHandlerContext ctx, HsfChannel channel, ChannelStateEvent e) {
		// send handshake request
		HandshakeRequest request = new HandshakeRequest(eventDispatcher.getService().getGroupName());
		ConnectorHandshakeProcessor handshakeProcessor = ((HsfConnector) eventDispatcher.getService())
				.getHandshakeProcessor();
		if (handshakeProcessor != null) {
			request.setAttachment(handshakeProcessor.getRequestAttachment());
		}

		//
		logger.warn("send handshake request({}) to channel({})", request, channel);
		// start timer
		HandshakeUtil.resetHandshakeTimeout(channel);
		//
		channel.write(request);

		return super.channelConnected(ctx, channel, e);
	}

	@Override
	public EventBehavior messageReceived(ChannelHandlerContext ctx, HsfChannel channel, MessageEvent e) {
		//
		if (e.getMessage() instanceof HandshakeAck) {
			// cancel timeout
			HandshakeUtil.cancelHandshakeTimeout(channel);

			//
			HandshakeAck ack = (HandshakeAck) e.getMessage();
			logger.warn("received handshake ack({}) from channel({})", ack, channel);

			//
			String groupName = ack.getGroupName();
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

			ConnectorHandshakeProcessor handshakeProcessor = ((HsfConnector) eventDispatcher.getService())
					.getHandshakeProcessor();

			// dispatch group created event
			if (isCreate) {
				//
				if (handshakeProcessor != null) {
					try {
						Map<String, Object> initAttrMap = handshakeProcessor.getInitAttributes();
						if (initAttrMap != null) {
							channelGroup.getAttributes().putAll(initAttrMap);
						}
						//
						handshakeProcessor.process(ack);
					} catch (Exception ex) {
						eventDispatcher.dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
					}
				}
				//
				eventDispatcher.dispatchGroupCreatedEvent(ctx, channel, groupName);
				//
				channelGroup.setPrepared(true);
			}
			// write handshake finish
			HandshakeFinish finish = new HandshakeFinish(service.getGroupName());
			if (handshakeProcessor != null) {
				finish.setAttachment(handshakeProcessor.getFinishAttachment());
			}
			channel.write(finish);
			//
			logger.warn("send handshake finish({}) to channel({})", finish, channel);
		}

		return EventBehavior.Continue;
	}
}
