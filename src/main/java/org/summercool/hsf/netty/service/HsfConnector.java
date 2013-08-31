package org.summercool.hsf.netty.service;

import java.net.SocketAddress;

import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.handshake.ConnectorHandshakeProcessor;

/**
 * @Title: HsfConnector.java
 * @Package org.summercool.hsf.netty.service
 * @Description: Hsf客户端服务接口
 * @author 简道
 * @date 2011-9-27 上午11:55:21
 * @version V1.0
 */
public interface HsfConnector extends HsfService {
	/**
	 * @Title: connect
	 * @Description: 同步连接到远程服务
	 * @author 简道
	 * @param addressArray
	 *        远程服务地址
	 * @return MultiChannelFuture 返回类型
	 */
	ChannelGroupFuture connect(SocketAddress... addressArray);

	/**
	 * @Title: connect
	 * @Description: 连接到远程服务
	 * @author 简道
	 * @param addressArray
	 *        远程服务地址
	 * @param sync
	 *        是否同步
	 * @return MultiChannelFuture 返回类型
	 */
	ChannelGroupFuture connect(SocketAddress[] addressArray, boolean sync);

	/**
	 * @Title: shutdown
	 * @Description: 关闭指定连接
	 * @author 简道
	 * @param ip
	 * @param port
	 * @return void 返回类型
	 */
	void shutdown(SocketAddress address);

	/**
	 * @Title: refreshIPList
	 * @Description: 刷新IP列表，为新增的IP建立连接，为缺失的IP断开连接，地址为null或empty，则不处理
	 * @author 简道
	 * @param addressArray
	 * @return void 返回类型
	 */
	void refreshIPList(SocketAddress... addressArray);

	/**
	 * @Title: refreshIPList
	 * @Description: 刷新IP列表，为新增的IP建立连接，为缺失的IP断开连接，forceProcess为false时，地址为null或empty，则不处理
	 * @author 简道
	 * @param forceProcess
	 *        是否开启强制模式，一旦打开，如果addressArray为null或empty，将关闭所有连接
	 * @param addressArray
	 * @return void 返回类型
	 */
	void refreshIPList(boolean forceProcess, SocketAddress... addressArray);

	ConnectorHandshakeProcessor getHandshakeProcessor();

	void setHandshakeProcessor(ConnectorHandshakeProcessor handshakeProcessor);
}
