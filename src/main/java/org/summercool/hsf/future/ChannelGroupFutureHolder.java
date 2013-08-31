package org.summercool.hsf.future;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Title: ChannelGroupFutureHolder.java
 * @Package org.summercool.hsf.future
 * @Description: 
 * @author 简道
 * @date 2012-2-23 上午01:28:38
 * @version V1.0
 */
public class ChannelGroupFutureHolder {
	private static final ConcurrentHashMap<Integer, ChannelGroupFuture> map = new ConcurrentHashMap<Integer, ChannelGroupFuture>();

	public static ChannelGroupFuture put(int channelId, ChannelGroupFuture future) {
		return map.put(channelId, future);
	}

	public static ChannelGroupFuture get(int channelId) {
		return map.get(channelId);
	}

	public static ChannelGroupFuture remove(int channelId) {
		return map.remove(channelId);
	}
}
