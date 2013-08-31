package org.summercool.hsf.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.service.HsfService;

/**
 * @Title: ContextHolder.java
 * @Package org.summercool.hsf.util
 * @Description: Hsf上下文执行类
 * @author 简道
 * @date 2011-11-15 下午7:02:56
 * @version V1.0
 */
public class HsfContextHolder {
	private static final String CHANNEL_PREFIX = "CHANNEL_";
	private static final String GROUP_NAME_PREFIX = "GROUP_NAME_";
	private static final String SERVICE_PREFIX = "SERVICE_";
	private static final String PROCESSING_GROUP_CREATED_EVENT_PREFIX = "PROCESSING_GROUP_CREATED_";

	/**
	 * @Title: setChannel
	 * @Description: 设置当前线程持有该Channel，channel为null时，删除该Channel
	 * @author 简道
	 * @param channel
	 * @return void 返回类型
	 */
	public static void setChannel(HsfChannel channel) {
		if (channel == null) {
			TLSUtil.remove(CHANNEL_PREFIX + Thread.currentThread().getId());
		} else {
			TLSUtil.setData(CHANNEL_PREFIX + Thread.currentThread().getId(), channel);
		}
	}

	/**
	 * @Title: getChannel
	 * @Description: 获取当前线程指持有的Channel
	 * @author 简道
	 * @return HsfChannel 返回类型
	 */
	public static HsfChannel getChannel() {
		return (HsfChannel) TLSUtil.getData(CHANNEL_PREFIX + Thread.currentThread().getId());
	}

	/**
	 * @Title: setRemoteGroupName
	 * @Description: 设置当前线程持有该Group，Group为null时，删除该Group
	 * @author 简道
	 * @param channel
	 * @return void 返回类型
	 */
	public static void setRemoteGroupName(String groupName) {
		if (groupName == null) {
			TLSUtil.remove(GROUP_NAME_PREFIX + Thread.currentThread().getId());
		} else {
			TLSUtil.setData(GROUP_NAME_PREFIX + Thread.currentThread().getId(), groupName);
		}
	}

	/**
	 * @Title: getRemoteGroupName
	 * @Description: 获取当前线程持有的远程GroupName
	 * @author 简道
	 * @return String 返回类型
	 */
	public static String getRemoteGroupName() {
		return (String) TLSUtil.getData(GROUP_NAME_PREFIX + Thread.currentThread().getId());
	}

	/**
	 * @Title: getHsfService
	 * @Description: 获取当前线程持有的HsfService
	 * @author 简道
	 * @return HsfService 返回类型
	 */
	public static HsfService getHsfService() {
		return (HsfService) TLSUtil.getData(SERVICE_PREFIX + Thread.currentThread().getId());
	}

	/**
	 * @Title: setHsfService
	 * @Description: 设置当前线程持有该HsfService
	 * @author 简道
	 * @param hsfService
	 * @return void 返回类型
	 */
	public static void setHsfService(HsfService hsfService) {
		if (hsfService == null) {
			TLSUtil.remove(SERVICE_PREFIX + Thread.currentThread().getId());
		} else {
			TLSUtil.setData(SERVICE_PREFIX + Thread.currentThread().getId(), hsfService);
		}
	}

	/**
	 * @Title: getRemoteAddress
	 * @Description: 获取远程地址
	 * @author 简道
	 * @return SocketAddress 返回类型
	 */
	public static InetAddress getRemoteAddress() {
		HsfChannel channel = getChannel();
		return ((InetSocketAddress) channel.getRemoteAddress()).getAddress();
	}

	/**
	 * @Title: isInProcessingGroupCreatedEvent
	 * @Description: 判断当前线程是否正在处理GroupCreated事件
	 * @author Kolor
	 * @return boolean 返回类型
	 */
	public static boolean isInProcessingGroupCreatedEvent() {
		Object value = TLSUtil.getData(PROCESSING_GROUP_CREATED_EVENT_PREFIX + Thread.currentThread().getId());
		if (value == null) {
			return false;
		}

		return (Boolean) value;
	}

	/**
	 * @Title: setInProcessingGroupCreatedEvent
	 * @Description: 设置当前线程是否正在处理GroupCreated事件
	 * @author 简道
	 * @param inProcessingGroupEvent
	 * @return void 返回类型
	 */
	public static void setInProcessingGroupCreatedEvent(boolean inProcessingGroupEvent) {
		TLSUtil.setData(PROCESSING_GROUP_CREATED_EVENT_PREFIX + Thread.currentThread().getId(), inProcessingGroupEvent);
	}

	/**
	 * @Title: getAttributes
	 * @Description: 获取Attributes
	 * @author 简道
	 * @return Map<String,Object> 返回类型
	 */
	public static Map<String, Object> getAttributes() {
		HsfService hsfService = getHsfService();
		if (hsfService == null) {
			return null;
		}

		String groupName = getRemoteGroupName();
		if (groupName == null) {
			return null;
		}

		HsfChannelGroup group = hsfService.getGroups().get(groupName);
		if (group == null) {
			return null;
		}

		return group.getAttributes();
	}
}
