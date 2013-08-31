package org.summercool.hsf.netty.service;

import java.net.SocketAddress;
import java.util.List;

import org.jboss.netty.channel.Channel;

import org.summercool.hsf.netty.handshake.AcceptorHandshakeProcessor;

/**
 * @Title: HsfAcceptor.java
 * @Package org.summercool.hsf.netty.service
 * @Description: Hsf服务器端服务接口
 * @author 简道
 * @date 2011-9-27 下午12:07:17
 * @version V1.0
 */
public interface HsfAcceptor extends HsfService {

	/**
	 * @Title: bind
	 * @Description: 监听指定端口
	 * @author 简道
	 * @param portArray
	 *        端口数组
	 * @return List<Channel> 返回类型
	 */
	List<Channel> bind(int... portArray);

	/**
	 * @Title: bind
	 * @Description: 监听指定SocketAddress
	 * @author 简道
	 * @param addressArray
	 *        SocketAddress数组
	 * @return List<Channel> 返回类型
	 */
	List<Channel> bind(SocketAddress... addressArray);

	AcceptorHandshakeProcessor getHandshakeProcessor();

	void setHandshakeProcessor(AcceptorHandshakeProcessor handshakeProcessor);
}